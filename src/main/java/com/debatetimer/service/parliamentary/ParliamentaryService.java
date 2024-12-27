package com.debatetimer.service.parliamentary;

import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.controller.exception.errorcode.ClientErrorCode;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequests;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.repository.member.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Transactional
    public ParliamentaryTableResponse save(ParliamentaryTableCreateRequest tableCreateRequest, Long memberId) {
        Member member = memberRepository.getById(memberId);
        int debateDuration = tableCreateRequest.table().sumOfTime();

        ParliamentaryTable savedTable = saveTable(tableCreateRequest.info(), member, debateDuration);
        ParliamentaryTimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest.table(), savedTable);
        return new ParliamentaryTableResponse(savedTable, savedTimeBoxes);
    }

    private ParliamentaryTable saveTable(
            TableInfoCreateRequest tableInfoCreateRequest,
            Member member,
            int debateDuration
    ) {
        ParliamentaryTable table = tableInfoCreateRequest.toTable(member, debateDuration);
        return tableRepository.save(table);
    }

    @Transactional(readOnly = true)
    public ParliamentaryTableResponse findTable(long tableId, long memberId) {
        ParliamentaryTable table = findOwnedTable(tableId, memberId); //TODO getByMemberIdAndTableId
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        return new ParliamentaryTableResponse(table, timeBoxes);
    }

    @Transactional
    public void deleteTable(Long tableId, long memberId) {
        ParliamentaryTable table = findOwnedTable(tableId, memberId);
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        timeBoxRepository.deleteAllInBatch(timeBoxes.getTimeBoxes());
        tableRepository.delete(table);
    }

    @Transactional
    public ParliamentaryTableResponse updateTable(
            ParliamentaryTableCreateRequest tableCreateRequest,
            long tableId,
            long memberId
    ) {
        ParliamentaryTable existingTable = findOwnedTable(tableId, memberId);
        Member member = memberRepository.getById(memberId);
        int debateDuration = tableCreateRequest.table().sumOfTime();
        ParliamentaryTable renewedTable = tableCreateRequest.info().toTable(member, debateDuration);
        existingTable.update(renewedTable);

        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(existingTable);
        timeBoxRepository.deleteAllInBatch(timeBoxes.getTimeBoxes());

        ParliamentaryTimeBoxes savedTimeBoxes = saveTimeBoxes(tableCreateRequest.table(), existingTable);

        return new ParliamentaryTableResponse(existingTable, savedTimeBoxes);
    }

    private ParliamentaryTimeBoxes saveTimeBoxes(
            TimeBoxCreateRequests timeBoxCreateRequests,
            ParliamentaryTable table
    ) {
        ParliamentaryTimeBoxes timeBoxes = timeBoxCreateRequests.toTimeBoxes(table);
        List<ParliamentaryTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(timeBoxes.getTimeBoxes());
        return new ParliamentaryTimeBoxes(savedTimeBoxes);
    }

    private ParliamentaryTable findOwnedTable(long memberId, long tableId) {
        ParliamentaryTable table = tableRepository.getById(tableId);
        validateTableOwn(memberId, table);
        return table;
    }

    private void validateTableOwn(long memberId, ParliamentaryTable table) {
        if (!table.isOwn(memberId)) {
            throw new DTClientErrorException(ClientErrorCode.TABLE_OWNER_MISMATCHED);
        }
    }

    private ParliamentaryTimeBoxes findTimeBoxes(ParliamentaryTable table) {
        List<ParliamentaryTimeBox> timeBoxes = timeBoxRepository.findAllByParliamentaryTable(table);
        return new ParliamentaryTimeBoxes(timeBoxes);
    }
}
