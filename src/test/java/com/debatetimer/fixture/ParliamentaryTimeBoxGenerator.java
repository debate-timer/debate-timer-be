package com.debatetimer.fixture;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.repository.parliamentary.ParliamentaryTimeBoxRepository;
import org.springframework.stereotype.Component;

@Component
public class ParliamentaryTimeBoxGenerator {

    private final ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository;

    public ParliamentaryTimeBoxGenerator(ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository) {
        this.parliamentaryTimeBoxRepository = parliamentaryTimeBoxRepository;
    }

    public ParliamentaryTimeBox generate(ParliamentaryTable testTable, int sequence) {
        ParliamentaryTimeBox timeBox = new ParliamentaryTimeBox(testTable, sequence, Stance.PROS,
                ParliamentaryBoxType.OPENING, 180, 1);
        return parliamentaryTimeBoxRepository.save(timeBox);
    }
}
