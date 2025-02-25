package com.debatetimer.fixture;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import com.debatetimer.repository.time_based.TimeBasedTimeBoxRepository;
import org.springframework.stereotype.Component;

@Component
public class TimeBasedTimeBoxGenerator {

    private final TimeBasedTimeBoxRepository timeBasedTimeBoxRepository;

    public TimeBasedTimeBoxGenerator(TimeBasedTimeBoxRepository timeBasedTimeBoxRepository) {
        this.timeBasedTimeBoxRepository = timeBasedTimeBoxRepository;
    }

    public TimeBasedTimeBox generate(TimeBasedTable testTable, int sequence) {
        TimeBasedTimeBox timeBox = new TimeBasedTimeBox(testTable, sequence, Stance.PROS,
                TimeBasedBoxType.OPENING, 180, 1);
        return timeBasedTimeBoxRepository.save(timeBox);
    }
}
