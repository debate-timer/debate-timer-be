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
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomizeServiceV2 {

    private final CustomizeTableRepository tableRepository;
    private final CustomizeTimeBoxRepository timeBoxRepository;
    private final BellRepository bellRepository;

    @Transactional
    public CustomizeTableResponse save(CustomizeTableCreateRequest tableCreateRequest, Member member) {
        CustomizeTable table = tableCreateRequest.toTable(member);
        CustomizeTableEntity savedTable = tableRepository.save(new CustomizeTableEntity(table));

        return saveTimeBoxesAndBells(tableCreateRequest, savedTable.toDomain());
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findTable(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        CustomizeTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(tableEntity);
        List<CustomizeTimeBoxResponse> timeBoxResponses = timeBoxes.getTimeBoxes()
                .stream()
                .map(this::getTimeBoxResponse)
                .toList();
        return new CustomizeTableResponse(tableEntity.toDomain(), timeBoxResponses);
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
        return saveTimeBoxesAndBells(tableCreateRequest, existingTable.toDomain());
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        CustomizeTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(tableEntity);
        tableEntity.updateUsedAt();
        List<CustomizeTimeBoxResponse> timeBoxResponses = timeBoxes.getTimeBoxes()
                .stream()
                .map(this::getTimeBoxResponse)
                .toList();
        return new CustomizeTableResponse(tableEntity.toDomain(), timeBoxResponses);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        CustomizeTableEntity table = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));

        deleteBell(timeBoxRepository.findTableTimeBoxes(table));
        timeBoxRepository.deleteAllByTable(table);
        tableRepository.delete(table);
    }

    private CustomizeTableResponse saveTimeBoxesAndBells(
            CustomizeTableCreateRequest tableCreateRequest,
            CustomizeTable table
    ) {
        List<CustomizeTimeBoxCreateRequest> timeBoxCreateRequests = tableCreateRequest.table();
        List<CustomizeTimeBoxResponse> timeBoxResponses = IntStream.range(0, timeBoxCreateRequests.size())
                .mapToObj(i -> createTimeBoxResponse(timeBoxCreateRequests.get(i), table, i + 1))
                .toList();
        return new CustomizeTableResponse(table, timeBoxResponses);
    }

    private CustomizeTimeBoxResponse createTimeBoxResponse(
            CustomizeTimeBoxCreateRequest request,
            CustomizeTable table,
            int sequence
    ) {
        CustomizeTimeBox savedTimeBox = timeBoxRepository.save(request.toTimeBox(table, sequence));
        return createTimeBoxResponse(request.bell(), savedTimeBox);
    }

    private CustomizeTimeBoxResponse createTimeBoxResponse(List<BellRequest> bellRequests, CustomizeTimeBox timeBox) {
        if (timeBox.getBoxType().isTimeBased()) {
            return new CustomizeTimeBoxResponse(timeBox, null);
        }

        List<BellResponse> bellResponses = bellRequests
                .stream()
                .map(bellRequest -> new BellEntity(timeBox, bellRequest.time(), bellRequest.count()))
                .map(bellRepository::save)
                .map(bell -> new BellResponse(bell.getTime(), bell.getCount()))
                .toList();
        return new CustomizeTimeBoxResponse(timeBox, bellResponses);
    }

    private CustomizeTimeBoxResponse getTimeBoxResponse(CustomizeTimeBox timeBox) {
        if (timeBox.getBoxType().isTimeBased()) {
            return new CustomizeTimeBoxResponse(timeBox, null);
        }

        List<BellResponse> bellResponses = bellRepository.findByCustomizeTimeBox(timeBox)
                .stream()
                .map(bell -> new BellResponse(bell.getTime(), bell.getCount()))
                .toList();
        return new CustomizeTimeBoxResponse(timeBox, bellResponses);
    }

    private void deleteBell(CustomizeTimeBoxes savedCustomizeTimeBoxes) {
        bellRepository.deleteAllByCustomizeTimeBoxIn(savedCustomizeTimeBoxes.getTimeBoxes());
    }
}
