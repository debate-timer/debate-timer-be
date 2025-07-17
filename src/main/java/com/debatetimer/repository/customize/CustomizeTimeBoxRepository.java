package com.debatetimer.repository.customize;

import com.debatetimer.domain.customize.CustomizeTimeBoxEntities;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface CustomizeTimeBoxRepository extends Repository<CustomizeTimeBoxEntity, Long> {

    CustomizeTimeBoxEntity save(CustomizeTimeBoxEntity timeBox);

    @Transactional
    default List<CustomizeTimeBoxEntity> saveAll(List<CustomizeTimeBoxEntity> timeBoxes) {
        return timeBoxes.stream()
                .map(this::save)
                .toList();
    }

    List<CustomizeTimeBoxEntity> findAllByCustomizeTable(CustomizeTableEntity table);

    default CustomizeTimeBoxEntities findTableTimeBoxes(CustomizeTableEntity table) {
        List<CustomizeTimeBoxEntity> timeBoxes = findAllByCustomizeTable(table);
        return new CustomizeTimeBoxEntities(timeBoxes);
    }

    @Query("DELETE FROM CustomizeTimeBoxEntity ctb WHERE ctb.customizeTable.id = :tableId")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByTable(long tableId);
}
