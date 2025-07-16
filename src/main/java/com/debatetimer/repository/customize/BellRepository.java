package com.debatetimer.repository.customize;

import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface BellRepository extends Repository<BellEntity, Long> {

    BellEntity save(BellEntity bell);

    List<BellEntity> findByCustomizeTimeBox(CustomizeTimeBoxEntity customizeTimeBox);

    void deleteAllByCustomizeTimeBoxIn(List<CustomizeTimeBoxEntity> customizeTimeBoxes);

    List<BellEntity> findAllByCustomizeTimeBoxIn(List<CustomizeTimeBoxEntity> timeBoxes);
}
