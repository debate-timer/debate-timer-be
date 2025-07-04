package com.debatetimer.repository.customize;

import com.debatetimer.domain.customize.CustomizeTimeBoxes;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface CustomizeTimeBoxRepository extends Repository<CustomizeTimeBox, Long> {

    CustomizeTimeBox save(CustomizeTimeBox timeBox);

    @Transactional
    default List<CustomizeTimeBox> saveAll(List<CustomizeTimeBox> timeBoxes) {
        return timeBoxes.stream()
                .map(this::save)
                .toList();
    }

    List<CustomizeTimeBox> findAllByCustomizeTable(CustomizeTableEntity table);

    default CustomizeTimeBoxes findTableTimeBoxes(CustomizeTableEntity table) {
        List<CustomizeTimeBox> timeBoxes = findAllByCustomizeTable(table);
        return new CustomizeTimeBoxes(timeBoxes);
    }

    @Query("DELETE FROM CustomizeTimeBox ctb WHERE ctb.customizeTable = :table")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByTable(CustomizeTableEntity table);
}
