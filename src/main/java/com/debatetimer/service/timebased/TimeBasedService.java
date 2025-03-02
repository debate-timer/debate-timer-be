package com.debatetimer.service.timebased;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import com.debatetimer.dto.timebased.request.TimeBasedTableCreateRequest;
import com.debatetimer.dto.timebased.response.TimeBasedTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.timebased.TimeBasedTableRepository;
import com.debatetimer.repository.timebased.TimeBasedTimeBoxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeBasedService {

    private final TimeBasedTableRepository tableRepository;
    private final TimeBasedTimeBoxRepository timeBoxRepository;

    @Transactional
    public TimeBasedTableResponse save(TimeBasedTableCreateRequest tableCreateRequest, Member member) {
        TimeBasedTable table = tableCreateRequest.toTable(member);
        TimeBasedTable savedTable = tableRepository.save(table);

        TimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable);
        return new TimeBasedTableResponse(savedTable, savedTimeBoxes);
    }

    @Transactional(readOnly = true)
    public TimeBasedTableResponse findTable(long tableId, Member member) {
        TimeBasedTable table = getOwnerTable(tableId, member.getId());
        TimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        return new TimeBasedTableResponse(table, timeBoxes);
    }

    @Transactional
    public TimeBasedTableResponse updateTable(
            TimeBasedTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        TimeBasedTable existingTable = getOwnerTable(tableId, member.getId());
        TimeBasedTable renewedTable = tableCreateRequest.toTable(member);
        existingTable.update(renewedTable);

        TimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(existingTable);
        timeBoxRepository.deleteAll(timeBoxes.getTimeBoxes());
        TimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable);
        return new TimeBasedTableResponse(existingTable, savedTimeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        TimeBasedTable table = getOwnerTable(tableId, member.getId());
        TimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        timeBoxRepository.deleteAll(timeBoxes.getTimeBoxes());
        tableRepository.delete(table);
    }

    private TimeBoxes saveTimeBoxes(
            TimeBasedTableCreateRequest tableCreateRequest,
            TimeBasedTable table
    ) {
        TimeBoxes timeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<TimeBasedTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(timeBoxes.getTimeBoxes());
        return new TimeBoxes(savedTimeBoxes);
    }

    private TimeBasedTable getOwnerTable(long tableId, long memberId) {
        TimeBasedTable foundTable = tableRepository.getById(tableId);
        validateOwn(foundTable, memberId);
        return foundTable;
    }

    private void validateOwn(TimeBasedTable table, long memberId) {
        if (!table.isOwner(memberId)) {
            throw new DTClientErrorException(ClientErrorCode.NOT_TABLE_OWNER);
        }
    }
}
