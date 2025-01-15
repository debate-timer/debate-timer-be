package com.debatetimer.service.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParliamentaryService {

    private final ParliamentaryTableRepository tableRepository;
    private final ParliamentaryTimeBoxRepository timeBoxRepository;

    @Transactional
    public ParliamentaryTableResponse save(ParliamentaryTableCreateRequest tableCreateRequest, Member member) {
        ParliamentaryTable table = tableCreateRequest.toTable(member);
        ParliamentaryTable savedTable = tableRepository.save(table);

        ParliamentaryTimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable);
        return new ParliamentaryTableResponse(savedTable, savedTimeBoxes);
    }

    @Transactional(readOnly = true)
    public ParliamentaryTableResponse findTable(long tableId, Member member) {
        ParliamentaryTable table = getOwnerTable(tableId, member.getId());
        ParliamentaryTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        return new ParliamentaryTableResponse(table, timeBoxes);
    }

    @Transactional
    public ParliamentaryTableResponse updateTable(
            ParliamentaryTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        ParliamentaryTable existingTable = getOwnerTable(tableId, member.getId());
        ParliamentaryTable renewedTable = tableCreateRequest.toTable(member);
        existingTable.update(renewedTable);

        ParliamentaryTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(existingTable);
        timeBoxRepository.deleteAll(timeBoxes.getTimeBoxes());
        ParliamentaryTimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable);
        return new ParliamentaryTableResponse(existingTable, savedTimeBoxes);
    }

    @Transactional
    public void deleteTable(Long tableId, Member member) {
        ParliamentaryTable table = getOwnerTable(tableId, member.getId());
        ParliamentaryTimeBoxes timeBoxes = timeBoxRepository.findTableTimeBoxes(table);
        timeBoxRepository.deleteAll(timeBoxes.getTimeBoxes());
        tableRepository.delete(table);
    }

    private ParliamentaryTimeBoxes saveTimeBoxes(
            ParliamentaryTableCreateRequest tableCreateRequest,
            ParliamentaryTable table
    ) {
        ParliamentaryTimeBoxes timeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<ParliamentaryTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(timeBoxes.getTimeBoxes());
        return new ParliamentaryTimeBoxes(savedTimeBoxes);
    }

    private ParliamentaryTable getOwnerTable(long tableId, long memberId) {
        ParliamentaryTable foundTable = tableRepository.getById(tableId);
        validateOwn(foundTable, memberId);
        return foundTable;
    }

    private void validateOwn(ParliamentaryTable table, long memberId) {
        if (!table.isOwner(memberId)) {
            throw new DTClientErrorException(ClientErrorCode.NOT_TABLE_OWNER);
        }
    }
}
