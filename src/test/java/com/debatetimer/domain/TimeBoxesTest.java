package com.debatetimer.domain;

//class TimeBoxesTest {
//
//    @Nested
//    class SortedBySequence {
//
//        @Test
//        void 타임박스의_순서에_따라_정렬된다() {
//            Member member = new Member("default@gmail.com");
//            ParliamentaryTable testTable = new ParliamentaryTable(member, "토론 테이블", "주제", true, true);
//            ParliamentaryTimeBox firstBox = new ParliamentaryTimeBox(testTable, 1, Stance.PROS,
//                    ParliamentaryBoxType.OPENING, 300, 1);
//            ParliamentaryTimeBox secondBox = new ParliamentaryTimeBox(testTable, 2, Stance.PROS,
//                    ParliamentaryBoxType.OPENING, 300, 1);
//            List<ParliamentaryTimeBox> timeBoxes = new ArrayList<>(Arrays.asList(secondBox, firstBox));
//
//            TimeBoxes<ParliamentaryTimeBox> actual = new TimeBoxes<>(timeBoxes);
//
//            assertThat(actual.getTimeBoxes()).containsExactly(firstBox, secondBox);
//        }
//    }
//}
