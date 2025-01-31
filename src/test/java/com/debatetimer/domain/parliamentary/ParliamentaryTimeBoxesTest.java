package com.debatetimer.domain.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParliamentaryTimeBoxesTest {

    @Nested
    class SortedBySequence {

        @Test
        void 타임박스의_순서에_따라_정렬된다() {
            Member member = new Member("default@gmail.com");
            ParliamentaryTable testTable = new ParliamentaryTable(member, "토론 테이블", "주제", 1800);
            ParliamentaryTimeBox firstBox = new ParliamentaryTimeBox(testTable, 1, Stance.PROS, BoxType.OPENING, 300,
                    1);
            ParliamentaryTimeBox secondBox = new ParliamentaryTimeBox(testTable, 2, Stance.PROS, BoxType.OPENING, 300,
                    1);
            List<ParliamentaryTimeBox> timeBoxes = new ArrayList<>(Arrays.asList(secondBox, firstBox));

            ParliamentaryTimeBoxes actual = new ParliamentaryTimeBoxes(timeBoxes);

            assertThat(actual.getTimeBoxes()).containsExactly(firstBox, secondBox);
        }
    }
}
