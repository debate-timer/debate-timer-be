package com.debatetimer.service.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntities;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
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
public class CustomizeService {

    private final CustomizeTableRepository tableRepository;
    private final CustomizeTimeBoxRepository timeBoxRepository;
    private final BellRepository bellRepository;

    @Transactional
    public CustomizeTableResponse save(CustomizeTableCreateRequest tableCreateRequest, Member member) {
        CustomizeTable table = tableCreateRequest.toTable(member);
        List<CustomizeTimeBox> timeBoxes = tableCreateRequest.toTimeBoxList();

        CustomizeTableEntity savedTableEntity = tableRepository.save(new CustomizeTableEntity(table));
        saveTimeBoxes(savedTableEntity, timeBoxes);
        return new CustomizeTableResponse(savedTableEntity.toDomain(), timeBoxes);
    }

    @Transactional(readOnly = true)
    public CustomizeTableResponse findTable(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        List<CustomizeTimeBoxEntity> timeBoxEntityList = timeBoxRepository.findAllByCustomizeTable(tableEntity);
        List<BellEntity> bellEntityList = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntityList);
        CustomizeTimeBoxEntities timeBoxEntities = new CustomizeTimeBoxEntities(timeBoxEntityList, bellEntityList);

        return new CustomizeTableResponse(tableEntity.toDomain(), timeBoxEntities.toDomain());
    }

    @Transactional
    public CustomizeTableResponse updateTable(
            CustomizeTableCreateRequest tableCreateRequest,
            long tableId,
            Member member
    ) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        tableEntity.updateTable(tableCreateRequest.toTable(member));

        bellRepository.deleteAllByTable(tableEntity.getId());
        timeBoxRepository.deleteAllByTable(tableEntity.getId());
        List<CustomizeTimeBox> timeBoxes = tableCreateRequest.toTimeBoxList();
        saveTimeBoxes(tableEntity, timeBoxes);
        return new CustomizeTableResponse(tableEntity.toDomain(), timeBoxes);
    }

    @Transactional
    public CustomizeTableResponse updateUsedAt(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        List<CustomizeTimeBoxEntity> timeBoxEntityList = timeBoxRepository.findAllByCustomizeTable(tableEntity);
        List<BellEntity> bellEntityList = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntityList);
        CustomizeTimeBoxEntities timeBoxEntities = new CustomizeTimeBoxEntities(timeBoxEntityList, bellEntityList);

        tableEntity.updateUsedAt();
        CustomizeTable table = tableEntity.toDomain();
        List<CustomizeTimeBox> timeBoxes = timeBoxEntities.toDomain();
        return new CustomizeTableResponse(table, timeBoxes);
    }

    @Transactional
    public void deleteTable(long tableId, Member member) {
        CustomizeTableEntity table = tableRepository.getByIdAndMember(tableId, member);

        bellRepository.deleteAllByTable(table.getId());
        timeBoxRepository.deleteAllByTable(table.getId());
        tableRepository.delete(table);
    }

    private void saveTimeBoxes(CustomizeTableEntity tableEntity, List<CustomizeTimeBox> timeBoxes) {
        IntStream.range(0, timeBoxes.size())
                .forEach(i -> saveTimeBox(tableEntity, timeBoxes.get(i), i + 1));
    }

    private void saveTimeBox(CustomizeTableEntity tableEntity, CustomizeTimeBox timeBox, int sequence) {
        CustomizeTimeBoxEntity timeBoxEntity = timeBoxRepository.save(
                new CustomizeTimeBoxEntity(tableEntity, timeBox, sequence));
        timeBox.getBells()
                .forEach(bell -> bellRepository.save(new BellEntity(timeBoxEntity, bell)));
    }
}
