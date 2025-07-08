package com.debatetimer.repository.customize;

import com.debatetimer.entity.customize.Bell;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface BellRepository extends Repository<Bell, Long> {

    Bell save(Bell bell);

    List<Bell> findByCustomizeTimeBox(CustomizeTimeBox customizeTimeBox);

    void delete(Bell bell);
}
