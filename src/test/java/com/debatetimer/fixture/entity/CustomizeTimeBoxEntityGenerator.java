package com.debatetimer.fixture.entity;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.customize.CustomizeTimeBoxRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomizeTimeBoxEntityGenerator {

    private final CustomizeTimeBoxRepository customizeTimeBoxRepository;

    public CustomizeTimeBoxEntityGenerator(CustomizeTimeBoxRepository customizeTimeBoxRepository) {
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

    public CustomizeTimeBoxEntity generate(CustomizeTableEntity testTable,
                                           CustomizeBoxType boxType,
                                           int sequence,
                                           int time) {
        CustomizeTimeBoxEntity timeBox = new CustomizeTimeBoxEntity(
                testTable,
                sequence,
                Stance.PROS,
                "입론",
                boxType,
                time,
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
