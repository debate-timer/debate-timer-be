package com.debatetimer.service.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.response.BellResponse;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.dto.customize.response.CustomizeTimeBoxResponse;
import com.debatetimer.entity.customize.Bell;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.customize.BellRepository;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomizeService {

    private final CustomizeTableRepository tableRepository;
    private final CustomizeTimeBoxRepository timeBoxRepository;
    private final BellRepository bellRepository;

    @Transactional
    public CustomizeTableResponse save(CustomizeTableCreateRequest tableCreateRequest, Member member) {
        CustomizeTable table = tableCreateRequest.toTable(member);
        CustomizeTableEntity savedTable = tableRepository.save(new CustomizeTableEntity(table));

        CustomizeTimeBoxes savedCustomizeTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable.toDomain());

        saveBell(savedTable, savedCustomizeTimeBoxes);
        return getCustomizeTableResponse(savedTable, savedCustomizeTimeBoxes);
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findTable(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        CustomizeTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(tableEntity);

        return getCustomizeTableResponse(tableEntity, timeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateTable(
            CustomizeTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        CustomizeTableEntity existingTable = tableRepository.getByIdAndMember(tableId, member);
        CustomizeTable renewedTable = tableCreateRequest.toTable(member);
        existingTable.updateTable(renewedTable);

        timeBoxRepository.deleteAllByTable(existingTable);
        CustomizeTimeBoxes savedCustomizeTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable.toDomain());

        deleteBell(savedCustomizeTimeBoxes);
        saveBell(existingTable, savedCustomizeTimeBoxes);
        return getCustomizeTableResponse(existingTable, savedCustomizeTimeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        CustomizeTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(tableEntity);
        tableEntity.updateUsedAt();

        return getCustomizeTableResponse(tableEntity, timeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        CustomizeTableEntity table = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));

        deleteBell(timeBoxRepository.findTableTimeBoxes(table));

        timeBoxRepository.deleteAllByTable(table);
        tableRepository.delete(table);

    }

    private CustomizeTimeBoxes saveTimeBoxes(
            CustomizeTableCreateRequest tableCreateRequest,
            CustomizeTable table
    ) {
        CustomizeTimeBoxes customizeTimeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<CustomizeTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(
                customizeTimeBoxes.getTimeBoxes());
        return new CustomizeTimeBoxes(savedTimeBoxes);
    }

    @NotNull
    private CustomizeTableResponse getCustomizeTableResponse(
            CustomizeTableEntity savedTable,
            CustomizeTimeBoxes savedCustomizeTimeBoxes
    ) {
        List<CustomizeTimeBoxResponse> customizeTimeBoxResponses = savedCustomizeTimeBoxes.getTimeBoxes()
                .stream()
                .map(timeBox -> {
                    List<BellResponse> bell = null;
                    if (timeBox.getBoxType().isNotTimeBased() && (savedTable.isFinishBell() || savedTable.isWarningBell())) {
                        bell = new ArrayList<>();
                        if (savedTable.isWarningBell()) {
                            bell.add(new BellResponse(timeBox.getTime() - 30, 1));
                        }
                        if (savedTable.isFinishBell()) {
                            bell.add(new BellResponse(timeBox.getTime(), 2));
                        }
                    }
                    return new CustomizeTimeBoxResponse(
                            timeBox,
                            bell
                    );
                })
                .toList();
        return new CustomizeTableResponse(savedTable.toDomain(), customizeTimeBoxResponses);
    }

    private void saveBell(CustomizeTableEntity savedTable, CustomizeTimeBoxes savedCustomizeTimeBoxes) {
        if (savedTable.isFinishBell()) {
            savedCustomizeTimeBoxes.getTimeBoxes()
                    .stream()
                    .filter(timeBox -> timeBox.getBoxType().isNotTimeBased())
                    .forEach(timeBox -> bellRepository.save(new Bell(
                            timeBox,
                            timeBox.getTime(),
                            2
                    )));
        }
        if (savedTable.isWarningBell()) {
            savedCustomizeTimeBoxes.getTimeBoxes()
                    .stream()
                    .filter(timeBox -> timeBox.getBoxType().isNotTimeBased())
                    .filter(timeBox -> timeBox.getTime() >= 30)
                    .forEach(timeBox -> bellRepository.save(new Bell(
                            timeBox,
                            timeBox.getTime() - 30,
                            1
                    )));
        }
    }

    private void deleteBell(CustomizeTimeBoxes savedCustomizeTimeBoxes) {
        savedCustomizeTimeBoxes.getTimeBoxes()
                .stream()
                .filter(timeBox -> timeBox.getBoxType().isNotTimeBased())
                .forEach(timeBox ->
                        bellRepository.findByCustomizeTimeBox(timeBox)
                                .forEach(bellRepository::delete)
                );
    }
}
