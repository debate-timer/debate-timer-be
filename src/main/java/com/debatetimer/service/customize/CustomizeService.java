package com.debatetimer.service.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.BellRequest;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.dto.customize.response.BellResponse;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.dto.customize.response.CustomizeTimeBoxResponse;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.customize.BellRepository;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

        CustomizeTimeBoxes savedCustomizeTimeBoxes = saveTimeBoxesAndBells(tableCreateRequest, savedTable.toDomain());

        // TODO : 밑에 부분은 프론트 업데이트 후 삭제 예정
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

        deleteBell(timeBoxRepository.findTableTimeBoxes(existingTable));
        timeBoxRepository.deleteAllByTable(existingTable);
        CustomizeTimeBoxes savedCustomizeTimeBoxes = saveTimeBoxesAndBells(tableCreateRequest, existingTable.toDomain());

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

    private CustomizeTimeBoxes saveTimeBoxesAndBells(
            CustomizeTableCreateRequest tableCreateRequest,
            CustomizeTable table
    ) {
        // TODO : 밑에 부분은 프론트 업데이트 후 주석 풀기
        /*
        List<CustomizeTimeBoxCreateRequest> timeBoxCreateRequests = tableCreateRequest.table();
        List<CustomizeTimeBoxResponse> timeBoxResponses = IntStream.range(0, timeBoxCreateRequests.size())
                .mapToObj(i -> createTimeBoxResponse(timeBoxCreateRequests.get(i), table, i + 1))
                .toList();
        new CustomizeTableResponse(table, timeBoxResponses);
        */

        // TODO : 밑에 부분은 프론트 업데이트 후 삭제 예정
        CustomizeTimeBoxes customizeTimeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<CustomizeTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(
                customizeTimeBoxes.getTimeBoxes());
        return new CustomizeTimeBoxes(savedTimeBoxes);
    }

    private CustomizeTimeBoxResponse createTimeBoxResponse(
            CustomizeTimeBoxCreateRequest request,
            CustomizeTable table,
            int sequence
    ) {
        CustomizeTimeBox savedTimeBox = timeBoxRepository.save(request.toTimeBox(table, sequence));
        List<BellResponse> bellResponses = createBellResponses(request.bell(), savedTimeBox);
        return new CustomizeTimeBoxResponse(savedTimeBox, bellResponses);
    }

    private List<BellResponse> createBellResponses(List<BellRequest> bellRequests, CustomizeTimeBox savedTimeBox) {
        return Optional.ofNullable(bellRequests)
                .orElse(Collections.emptyList())
                .stream()
                .map(bellRequest -> new BellEntity(savedTimeBox, bellRequest.time(), bellRequest.count()))
                .map(bellRepository::save)
                .map(bell -> new BellResponse(bell.getTime(), bell.getCount()))
                .toList();
    }

    // TODO : 밑에 부분은 프론트 업데이트 후 삭제 예정
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

    // TODO : 밑에 부분은 프론트 업데이트 후 삭제 예정
    private void saveBell(CustomizeTableEntity savedTable, CustomizeTimeBoxes savedCustomizeTimeBoxes) {
        if (savedTable.isFinishBell()) {
            savedCustomizeTimeBoxes.getTimeBoxes()
                    .stream()
                    .filter(timeBox -> timeBox.getBoxType().isNotTimeBased())
                    .forEach(timeBox -> bellRepository.save(new BellEntity(
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
                    .forEach(timeBox -> bellRepository.save(new BellEntity(
                            timeBox,
                            timeBox.getTime() - 30,
                            1
                    )));
        }
    }

    private void deleteBell(CustomizeTimeBoxes savedCustomizeTimeBoxes) {
        bellRepository.deleteAllByCustomizeTimeBoxIn(savedCustomizeTimeBoxes.getTimeBoxes());
    }
}
