package com.debatetimer.domainrepository.customize;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.customize.BellRepository;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CustomizeTableDomainRepository {

    private final CustomizeTableRepository tableRepository;
    private final CustomizeTimeBoxRepository timeBoxRepository;
    private final BellRepository bellRepository;

    @Transactional
    public CustomizeTable save(CustomizeTable table, List<CustomizeTimeBox> timeBoxes) {
        CustomizeTableEntity savedTableEntity = tableRepository.save(new CustomizeTableEntity(table));
        saveTimeBoxes(savedTableEntity, timeBoxes);

        return savedTableEntity.toDomain();
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

    @Transactional(readOnly = true)
    public CustomizeTable getByIdAndMember(long tableId, Member member) {
        return tableRepository.getByIdAndMember(tableId, member)
                .toDomain();
    }

    @Transactional(readOnly = true)
    public CustomizeTable getById(long tableId) {
        return tableRepository.getById(tableId)
                .toDomain();
    }

    @Transactional(readOnly = true)
    public List<CustomizeTimeBox> getMemberCustomizeTimeBoxes(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        List<CustomizeTimeBoxEntity> timeBoxEntityList = timeBoxRepository.findAllByCustomizeTable(tableEntity);
        List<BellEntity> bellEntityList = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntityList);
        return toCustomizeTimeBoxes(timeBoxEntityList, bellEntityList);
    }

    @Transactional(readOnly = true)
    public List<CustomizeTimeBox> getCustomizeTimeBoxes(long tableId) {
        CustomizeTableEntity tableEntity = tableRepository.getById(tableId);
        List<CustomizeTimeBoxEntity> timeBoxEntityList = timeBoxRepository.findAllByCustomizeTable(tableEntity);
        List<BellEntity> bellEntityList = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntityList);
        return toCustomizeTimeBoxes(timeBoxEntityList, bellEntityList);
    }

    @Transactional(readOnly = true)
    public long getTotalTimeBoxTimes(long tableId) {
        return timeBoxRepository.sumTimeByTableId(tableId);
    }

    private List<CustomizeTimeBox> toCustomizeTimeBoxes(
            List<CustomizeTimeBoxEntity> timeBoxEntities,
            List<BellEntity> bellEntities
    ) {
        Map<Long, List<BellEntity>> timeBoxIdToBellEntities = bellEntities.stream()
                .collect(Collectors.groupingBy(bellEntity -> bellEntity.getCustomizeTimeBox().getId()));
        return timeBoxEntities.stream()
                .sorted(Comparator.comparing(CustomizeTimeBoxEntity::getSequence))
                .map(timeBoxEntity -> toTimeBox(
                        timeBoxEntity,
                        timeBoxIdToBellEntities.getOrDefault(timeBoxEntity.getId(), Collections.emptyList()))
                ).toList();
    }

    private CustomizeTimeBox toTimeBox(CustomizeTimeBoxEntity timeBoxEntity, List<BellEntity> bellEntities) {
        return bellEntities.stream()
                .map(BellEntity::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), timeBoxEntity::toDomain));
    }

    @Transactional
    public CustomizeTable update(CustomizeTable table, long tableId, Member member, List<CustomizeTimeBox> timeBoxes) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        tableEntity.updateTable(table);

        bellRepository.deleteAllByTable(tableEntity.getId());
        timeBoxRepository.deleteAllByTable(tableEntity.getId());

        saveTimeBoxes(tableEntity, timeBoxes);
        return tableEntity.toDomain();
    }

    @Transactional
    public CustomizeTable updateUsedAt(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        tableEntity.updateUsedAt();
        return tableEntity.toDomain();
    }

    @Transactional
    public void delete(long tableId, Member member) {
        CustomizeTableEntity table = tableRepository.getByIdAndMember(tableId, member);

        bellRepository.deleteAllByTable(table.getId());
        timeBoxRepository.deleteAllByTable(table.getId());
        tableRepository.delete(table);
    }
}
