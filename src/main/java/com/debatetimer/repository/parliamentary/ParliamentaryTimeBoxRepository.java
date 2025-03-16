package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.TimeBoxes;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface ParliamentaryTimeBoxRepository extends Repository<ParliamentaryTimeBox, Long> {

    ParliamentaryTimeBox save(ParliamentaryTimeBox timeBox);

    @Transactional
    default List<ParliamentaryTimeBox> saveAll(List<ParliamentaryTimeBox> timeBoxes) {
        return timeBoxes.stream()
                .map(this::save)
                .toList();
    }

    List<ParliamentaryTimeBox> findAllByParliamentaryTable(ParliamentaryTable table);

    default TimeBoxes<ParliamentaryTimeBox> findTableTimeBoxes(ParliamentaryTable table) {
        List<ParliamentaryTimeBox> timeBoxes = findAllByParliamentaryTable(table);
        return new TimeBoxes<>(timeBoxes);
    }

    @Query("DELETE FROM ParliamentaryTimeBox ptb WHERE ptb IN :timeBoxes")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteAll(List<ParliamentaryTimeBox> timeBoxes);
}
