package com.debatetimer.domainrepository.customize;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.customize.BellRepository;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import java.util.List;
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
    public List<CustomizeTimeBox> getCustomizeTimeBoxes(long tableId, Member member) {
        CustomizeTableEntity tableEntity = tableRepository.getByIdAndMember(tableId, member);
        List<CustomizeTimeBoxEntity> timeBoxEntityList = timeBoxRepository.findAllByCustomizeTable(tableEntity);
        List<BellEntity> bellEntityList = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntityList);
        return toCustomizeTimeBoxes(timeBoxEntityList, bellEntityList);
    }

    private List<CustomizeTimeBox> toCustomizeTimeBoxes(List<CustomizeTimeBoxEntity> timeBoxEntities,
                                                        List<BellEntity> bellEntities) {
        return timeBoxEntities.stream()
                .map(timebox -> timebox.toDomain(getBells(timebox, bellEntities)))
                .toList();
    }

    private List<Bell> getBells(CustomizeTimeBoxEntity timeBox, List<BellEntity> bells) {
        return bells.stream()
                .filter(bell -> bell.isContained(timeBox))
                .map(BellEntity::toDomain)
                .toList();
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
