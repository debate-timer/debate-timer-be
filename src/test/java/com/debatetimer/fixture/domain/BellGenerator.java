package com.debatetimer.fixture.domain;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.BellType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BellGenerator {

    public List<Bell> generate(int size) {
        List<Bell> bells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            bells.add(generate());
        }
        return bells;
    }

    private Bell generate() {
        return new Bell(BellType.AFTER_START, 10, 1);
    }
}
