package com.debatetimer.fixture.entity;

import com.debatetimer.domain.customize.BellType;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.customize.BellRepository;
import org.springframework.stereotype.Component;

@Component
public class BellEntityGenerator {

    private final BellRepository bellRepository;

    public BellEntityGenerator(BellRepository bellRepository) {
        this.bellRepository = bellRepository;
    }

    public BellEntity generate(CustomizeTimeBoxEntity timeBox, BellType type, int time, int count) {
        BellEntity bell = new BellEntity(timeBox, type, time, count);
        return bellRepository.save(bell);
    }
}
