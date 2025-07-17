package com.debatetimer.repository.customize;

import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface BellRepository extends Repository<BellEntity, Long> {

    BellEntity save(BellEntity bell);

    @Query("DELETE FROM BellEntity b WHERE b.customizeTimeBox.customizeTable.id = :tableId")
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByTable(long tableId);

    List<BellEntity> findAllByCustomizeTimeBoxIn(List<CustomizeTimeBoxEntity> timeBoxes);
}
