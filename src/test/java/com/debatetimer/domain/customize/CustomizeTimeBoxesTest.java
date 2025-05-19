package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomizeTimeBoxesTest {

    @Nested
    class SortedBySequence {

        @Test
        void 타임박스의_순서에_따라_정렬된다() {
            Member member = new Member("default@gmail.com");
            CustomizeTable testTable = new CustomizeTable(member, "토론 테이블", "주제",
                    true, true, "찬성", "반대");
            CustomizeTimeBox firstBox = new CustomizeTimeBox(testTable, 1, Stance.PROS, "입론",
                    CustomizeBoxType.NORMAL, 300, "콜리");
            CustomizeTimeBox secondBox = new CustomizeTimeBox(testTable, 2, Stance.PROS, "입론",
                    CustomizeBoxType.NORMAL, 300, "콜리2");
            List<CustomizeTimeBox> timeBoxes = new ArrayList<>(Arrays.asList(secondBox, firstBox));

            CustomizeTimeBoxes actual = new CustomizeTimeBoxes(timeBoxes);

            assertThat(actual.getTimeBoxes()).containsExactly(firstBox, secondBox);
        }
    }
}
