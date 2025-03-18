package com.debatetimer.service.customize;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
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

        TimeBoxes<CustomizeTimeBox> savedTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable);
        return new CustomizeTableResponse(savedTable, savedTimeBoxes);
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findTable(long tableId, Member member) {
        CustomizeTable table = getOwnerTable(tableId, member.getId());
        TimeBoxes<CustomizeTimeBox> timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateTable(
            CustomizeTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        CustomizeTable existingTable = getOwnerTable(tableId, member.getId());
        CustomizeTable renewedTable = tableCreateRequest.toTable(member);
        existingTable.update(renewedTable);

        TimeBoxes<CustomizeTimeBox> timeBoxes = timeBoxRepository.findTableTimeBoxes(existingTable);
        timeBoxRepository.deleteAll(timeBoxes.getTimeBoxes());
        TimeBoxes<CustomizeTimeBox> savedTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable);
        return new CustomizeTableResponse(existingTable, savedTimeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTable table = getOwnerTable(tableId, member.getId());
        TimeBoxes<CustomizeTimeBox> timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        table.updateUsedAt();

        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        CustomizeTable table = getOwnerTable(tableId, member.getId());
        TimeBoxes<CustomizeTimeBox> timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        timeBoxRepository.deleteAll(timeBoxes.getTimeBoxes());
        tableRepository.delete(table);
    }

    private TimeBoxes<CustomizeTimeBox> saveTimeBoxes(
            CustomizeTableCreateRequest tableCreateRequest,
            CustomizeTable table
    ) {
        TimeBoxes<CustomizeTimeBox> timeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<CustomizeTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(timeBoxes.getTimeBoxes());
        return new TimeBoxes<>(savedTimeBoxes);
    }

    private CustomizeTable getOwnerTable(long tableId, long memberId) {
        CustomizeTable foundTable = tableRepository.getById(tableId);
        validateOwn(foundTable, memberId);
        return foundTable;
    }

    private void validateOwn(CustomizeTable table, long memberId) {
        if (!table.isOwner(memberId)) {
            throw new DTClientErrorException(ClientErrorCode.NOT_TABLE_OWNER);
        }
    }
}
