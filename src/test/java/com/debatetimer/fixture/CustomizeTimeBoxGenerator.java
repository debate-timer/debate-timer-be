package com.debatetimer.fixture;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomizeTimeBoxGenerator {

    private final CustomizeTimeBoxRepository customizeTimeBoxRepository;

    public CustomizeTimeBoxGenerator(CustomizeTimeBoxRepository customizeTimeBoxRepository) {
        this.customizeTimeBoxRepository = customizeTimeBoxRepository;
    }

    public CustomizeTimeBoxEntity generate(CustomizeTableEntity testTable, CustomizeBoxType boxType, int sequence) {
        CustomizeTimeBoxEntity timeBox = new CustomizeTimeBoxEntity(
                testTable,
                sequence,
                Stance.PROS,
                "입론",
                boxType,
                180,
                "콜리"
        );
        return customizeTimeBoxRepository.save(timeBox);
    }

    public CustomizeTimeBoxEntity generateNotExistSpeaker(CustomizeTableEntity testTable, CustomizeBoxType boxType,
                                                          int sequence) {
        CustomizeTimeBoxEntity timeBox = new CustomizeTimeBoxEntity(
                testTable,
                sequence,
                Stance.PROS,
                "입론",
                boxType,
                180,
                null
        );
        return customizeTimeBoxRepository.save(timeBox);
    }
}
