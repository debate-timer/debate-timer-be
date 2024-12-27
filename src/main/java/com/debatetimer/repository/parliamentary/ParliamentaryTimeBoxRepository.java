package com.debatetimer.repository.parliamentary;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParliamentaryTimeBoxRepository extends JpaRepository<ParliamentaryTimeBox, Long> {

    List<ParliamentaryTimeBox> findAllByParliamentaryTable(ParliamentaryTable table);
}
