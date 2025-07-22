package com.debatetimer.service.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntities;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
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
        CustomizeTableEntity savedTable = tableRepository.save(new CustomizeTableEntity(table));

        CustomizeTimeBoxEntities savedCustomizeTimeBoxes = saveTimeBoxes(tableCreateRequest, savedTable.toDomain());
        return new CustomizeTableResponse(savedTable.toDomain(), savedCustomizeTimeBoxes);
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findTable(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        CustomizeTimeBoxEntities timeBoxes = timeBoxRepository.findTableTimeBoxes(tableEntity);
        return new CustomizeTableResponse(tableEntity.toDomain(), timeBoxes);
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

        timeBoxRepository.deleteAllByTable(existingTable.getId());
        CustomizeTimeBoxEntities savedCustomizeTimeBoxes = saveTimeBoxes(tableCreateRequest, existingTable.toDomain());
        return new CustomizeTableResponse(existingTable.toDomain(), savedCustomizeTimeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        CustomizeTimeBoxEntities timeBoxes = timeBoxRepository.findTableTimeBoxes(tableEntity);
        tableEntity.updateUsedAt();

        return new CustomizeTableResponse(tableEntity.toDomain(), timeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        CustomizeTableEntity table = tableRepository.getByIdAndMember(tableId, member);
        timeBoxRepository.deleteAllByTable(table.getId());
        tableRepository.delete(table);
    }

    private CustomizeTimeBoxEntities saveTimeBoxes(
            CustomizeTableCreateRequest tableCreateRequest,
            CustomizeTable table
    ) {
        CustomizeTimeBoxEntities customizeTimeBoxes = tableCreateRequest.toTimeBoxes(table);
        List<CustomizeTimeBoxEntity> savedTimeBoxes = timeBoxRepository.saveAll(
                customizeTimeBoxes.getTimeBoxes());
        return new CustomizeTimeBoxEntities(savedTimeBoxes);
    }
}
