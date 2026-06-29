package com.debatetimer.service.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomizeService {

    private final CustomizeTableDomainRepository customizeTableDomainRepository;

    @Transactional
    public CustomizeTableResponse save(CustomizeTableCreateRequest tableCreateRequest, Member member) {
        CustomizeTable table = tableCreateRequest.toTable(member);
        List<CustomizeTimeBox> timeBoxes = tableCreateRequest.toTimeBoxes();
        CustomizeTable savedTable = customizeTableDomainRepository.save(table, timeBoxes);
        return new CustomizeTableResponse(savedTable, timeBoxes);
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findMemberTable(long tableId, Member member) {
        CustomizeTable table = customizeTableDomainRepository.getByIdAndMember(tableId, member);
        List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getCustomizeTimeBoxes(tableId, member);
        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional(readOnly = true)
    public long findDebateTime(long tableId, Member member) {
        CustomizeTable customizeTable = customizeTableDomainRepository.getByIdAndMember(tableId, member);
        return customizeTableDomainRepository.getTotalTimeBoxTimes(customizeTable.getId());
    }

    @Transactional
    public CustomizeTableResponse updateTable(
            CustomizeTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        CustomizeTable table = tableCreateRequest.toTable(member);
        List<CustomizeTimeBox> timeBoxes = tableCreateRequest.toTimeBoxes();

        CustomizeTable updatedTable = customizeTableDomainRepository.update(table, tableId, member, timeBoxes);
        return new CustomizeTableResponse(updatedTable, timeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTable table = customizeTableDomainRepository.updateUsedAt(tableId, member);
        List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getCustomizeTimeBoxes(tableId, member);
        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        customizeTableDomainRepository.delete(tableId, member);
    }
}
