package com.debatetimer.fixture;

import com.debatetimer.domain.customize.BellType;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.customize.BellRepository;
import org.springframework.stereotype.Component;

@Component
public class BellGenerator {

    private final BellRepository bellRepository;

    public BellGenerator(BellRepository bellRepository) {
        this.bellRepository = bellRepository;
    }

    public BellEntity generate(CustomizeTimeBoxEntity timeBox, BellType type, int time, int count) {
        BellEntity bell = new BellEntity(timeBox, type, time, count);
        return bellRepository.save(bell);
    }
}
