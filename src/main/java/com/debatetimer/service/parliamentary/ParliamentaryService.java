package com.debatetimer.service.parliamentary;

import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.controller.exception.errorcode.ClientErrorCode;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParliamentaryService {

    private final ParliamentaryTableRepository tableRepository;
    private final ParliamentaryTimeBoxRepository timeBoxRepository;
    private final EntityManager entityManager;

    @Transactional
    public ParliamentaryTableResponse save(ParliamentaryTableCreateRequest tableCreateRequest, Member member) {
        ParliamentaryTable table = tableCreateRequest.toTable(member);
        ParliamentaryTable savedTable = tableRepository.save(table);

        ParliamentaryTimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable);
        return new ParliamentaryTableResponse(savedTable, savedTimeBoxes);
    }

    @Transactional(readOnly = true)
    public ParliamentaryTableResponse findTable(long tableId, Member member) {
        ParliamentaryTable table = findOwnedTable(member, tableId);
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        return new ParliamentaryTableResponse(table, timeBoxes);
    }

    @Transactional
    public ParliamentaryTableResponse updateTable(
            ParliamentaryTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        ParliamentaryTable existingTable = findOwnedTable(member, tableId);
        ParliamentaryTable renewedTable = tableCreateRequest.toTable(member);
        existingTable.update(renewedTable);

        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(existingTable);
        timeBoxRepository.deleteAllInBatch(timeBoxes.getTimeBoxes());

        ParliamentaryTimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable);

        return new ParliamentaryTableResponse(existingTable, savedTimeBoxes);
    }

    @Transactional
    public void deleteTable(Long tableId, Member member) {
        ParliamentaryTable table = findOwnedTable(member, tableId);
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        timeBoxRepository.deleteAllInBatch(timeBoxes.getTimeBoxes());
        entityManager.clear();
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

    private ParliamentaryTable findOwnedTable(Member member, long tableId) {
        return tableRepository.findByIdAndMemberId(tableId, member.getId())
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.MEMBER_TABLE_NOT_FOUND));
    }

    private ParliamentaryTimeBoxes findTimeBoxes(ParliamentaryTable table) {
        List<ParliamentaryTimeBox> timeBoxes = timeBoxRepository.findAllByParliamentaryTable(table);
        return new ParliamentaryTimeBoxes(timeBoxes);
    }
}
