package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParliamentaryTimeBoxRepository extends JpaRepository<ParliamentaryTimeBox, Long> {

    default ParliamentaryTimeBoxes findTableTimeBoxes(ParliamentaryTable table) {
        List<ParliamentaryTimeBox> timeBoxes = findAllByParliamentaryTable(table);
        return new ParliamentaryTimeBoxes(timeBoxes);
    }

    List<ParliamentaryTimeBox> findAllByParliamentaryTable(ParliamentaryTable table);
}
