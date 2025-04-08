package com.debatetimer.repository.timebased;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface TimeBasedTimeBoxRepository extends Repository<TimeBasedTimeBox, Long> {

    TimeBasedTimeBox save(TimeBasedTimeBox timeBox);

    @Transactional
    default List<TimeBasedTimeBox> saveAll(List<TimeBasedTimeBox> timeBoxes) {
        return timeBoxes.stream()
                .map(this::save)
                .toList();
    }

    List<TimeBasedTimeBox> findAllByTimeBasedTable(TimeBasedTable table);

    default TimeBoxes<TimeBasedTimeBox> findTableTimeBoxes(TimeBasedTable table) {
        List<TimeBasedTimeBox> timeBoxes = findAllByTimeBasedTable(table);
        return new TimeBoxes<>(timeBoxes);
    }

    @Query("DELETE FROM TimeBasedTimeBox tbtb WHERE tbtb IN :timeBoxes")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteAll(List<TimeBasedTimeBox> timeBoxes);
}
