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

    // 시간 총량제 타임 박스는 time이 비어 있을 수 있으므로 양 팀 시간(팀당 시간 x 2)으로 대신 계산
    @Query("""
            SELECT COALESCE(SUM(COALESCE(ctb.time, ctb.timePerTeam * 2, 0)), 0)
            FROM CustomizeTimeBoxEntity ctb
            WHERE ctb.customizeTable.id = :tableId
            """)
    long sumTimeByTableId(long tableId);

    @Query("DELETE FROM CustomizeTimeBoxEntity ctb WHERE ctb.customizeTable.id = :tableId")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByTable(long tableId);
}
