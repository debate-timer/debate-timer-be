package com.debatetimer.service.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomizeService {

    private final CustomizeTableRepository tableRepository;
    private final CustomizeTimeBoxRepository timeBoxRepository;

    @Transactional
    public CustomizeTableResponse save(CustomizeTableCreateRequest tableCreateRequest, Member member) {
        CustomizeTable table = tableCreateRequest.toTable(member);
        CustomizeTable savedTable = tableRepository.save(table);

        CustomizeTimeBoxes savedCustomizeTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable);
        return new CustomizeTableResponse(savedTable, savedCustomizeTimeBoxes);
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findTable(long tableId, Member member) {
        CustomizeTable table = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        CustomizeTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateTable(
            CustomizeTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        CustomizeTable existingTable = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        CustomizeTable renewedTable = tableCreateRequest.toTable(member);
        existingTable.updateTable(renewedTable);

        timeBoxRepository.deleteAllByTable(existingTable);
        CustomizeTimeBoxes savedCustomizeTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable);
        return new CustomizeTableResponse(existingTable, savedCustomizeTimeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTable table = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        CustomizeTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        table.updateUsedAt();

        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        CustomizeTable table = tableRepository.findByIdAndMember(tableId, member)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.TABLE_NOT_FOUND));
        timeBoxRepository.deleteAllByTable(table);
        tableRepository.delete(table);
    }

    private CustomizeTimeBoxes saveTimeBoxes(
            CustomizeTableCreateRequest tableCreateRequest,
            CustomizeTable table
    ) {
        CustomizeTimeBoxes customizeTimeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<CustomizeTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(customizeTimeBoxes.getTimeBoxes());
        return new CustomizeTimeBoxes(savedTimeBoxes);
    }
}
