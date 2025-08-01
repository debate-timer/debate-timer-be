package com.debatetimer.fixture.domain;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.customize.NormalTimeBox;
import com.debatetimer.domain.customize.Stance;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomizeTimeBoxGenerator {

    public CustomizeTimeBox generate(List<Bell> bells) {
        return new NormalTimeBox(
                Stance.PROS,
                "입론",
                "콜리",
                10,
                bells
        );
    }

    public CustomizeTimeBox generate(List<Bell> bells, String speechType) {
        return new NormalTimeBox(
                Stance.PROS,
                speechType,
                "콜리",
                10,
                bells
        );
    }
}
