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

@Service
@RequiredArgsConstructor
public class ParliamentaryService {

    private final ParliamentaryTableRepository tableRepository;
    private final ParliamentaryTimeBoxRepository timeBoxRepository;
    private final MemberRepository memberRepository;

    public ParliamentaryTableResponse save(ParliamentaryTableCreateRequest tableCreateRequest, Long memberId) {
        Member member = memberRepository.getById(memberId);
        int debateDuration = tableCreateRequest.table().sumOfTime();

        ParliamentaryTable savedTable = saveTable(tableCreateRequest.info(), member, debateDuration);
        saveTimeBoxes(tableCreateRequest.table(), savedTable);
        return findTable(savedTable.getId(), memberId);
    }

    private ParliamentaryTable saveTable(TableInfoCreateRequest tableInfoCreateRequest, Member member, int debateDuration) {
        ParliamentaryTable table = tableInfoCreateRequest.toTable(member, debateDuration);
        return tableRepository.save(table);
    }

    private ParliamentaryTimeBoxes saveTimeBoxes(TimeBoxCreateRequests timeBoxCreateRequests, ParliamentaryTable table) {
        ParliamentaryTimeBoxes timeBoxes = timeBoxCreateRequests.toTimeBoxes(table);
        List<ParliamentaryTimeBox> savedTimeBoxes = timeBoxRepository.saveAll(timeBoxes.getTimeBoxes());
        return new ParliamentaryTimeBoxes(savedTimeBoxes);
    }

    public ParliamentaryTableResponse findTable(long tableId, long memberId) {
        ParliamentaryTable table = tableRepository.getById(tableId); //TODO getByMemberIdAndTableId
        validateTableOwn(memberId, table);
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        return new ParliamentaryTableResponse(table, timeBoxes);
    }

    public void deleteTable(Long tableId, Long memberId) {
        ParliamentaryTable table = tableRepository.getById(tableId);
        validateTableOwn(memberId, table);
        ParliamentaryTimeBoxes timeBoxes = findTimeBoxes(table);
        timeBoxRepository.deleteAllInBatch(timeBoxes.getTimeBoxes());
        tableRepository.delete(table);
    }

    private ParliamentaryTimeBoxes findTimeBoxes(ParliamentaryTable table) {
        List<ParliamentaryTimeBox> timeBoxes = timeBoxRepository.findAllByParliamentaryTable(table);
        return new ParliamentaryTimeBoxes(timeBoxes);
    }

    private void validateTableOwn(long memberId, ParliamentaryTable table) {
        if (!table.isOwn(memberId)) {
            throw new DTClientErrorException(ClientErrorCode.TABLE_OWNER_MISMATCHED);
        }
    }
}
