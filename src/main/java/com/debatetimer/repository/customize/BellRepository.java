package com.debatetimer.repository.customize;

import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface BellRepository extends Repository<BellEntity, Long> {

    BellEntity save(BellEntity bell);

    List<BellEntity> findByCustomizeTimeBox(CustomizeTimeBox customizeTimeBox);

    void delete(BellEntity bell);

    void deleteAllByCustomizeTimeBoxIn(List<CustomizeTimeBox> customizeTimeBoxes);

    List<BellEntity> findAllByCustomizeTimeBoxIn(List<CustomizeTimeBox> timeBoxes);

    List<BellEntity> findAll();
}
