package com.debatetimer.repository.customize;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
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

    List<CustomizeTimeBox> findAllByCustomizeTable(CustomizeTable table);

    default TimeBoxes<CustomizeTimeBox> findTableTimeBoxes(CustomizeTable table) {
        List<CustomizeTimeBox> timeBoxes = findAllByCustomizeTable(table);
        return new TimeBoxes<>(timeBoxes);
    }

    @Query("DELETE FROM CustomizeTimeBox ptb WHERE ptb IN :timeBoxes")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteAll(List<CustomizeTimeBox> timeBoxes);
}
