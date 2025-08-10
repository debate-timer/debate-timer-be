package com.debatetimer.repository.customize;

import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface CustomizeTimeBoxRepository extends Repository<CustomizeTimeBoxEntity, Long> {

    CustomizeTimeBoxEntity save(CustomizeTimeBoxEntity timeBox);

    List<CustomizeTimeBoxEntity> findAllByCustomizeTable(CustomizeTableEntity table);

    @Query("DELETE FROM CustomizeTimeBoxEntity ctb WHERE ctb.customizeTable.id = :tableId")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByTable(long tableId);
}
