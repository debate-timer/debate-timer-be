package com.debatetimer.domain.timebased;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.dto.member.TableType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TimeBasedTableTest {

    @Nested
    class GetType {

        @Test
        void 시간총량제_테이블_타입을_반환한다() {
            TimeBasedTable timeBasedTable = new TimeBasedTable();

            assertThat(timeBasedTable.getType()).isEqualTo(TableType.TIME_BASED);
        }
    }
}
