package com.debatetimer.domain.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.dto.member.TableType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParliamentaryTableTest {

    @Nested
    class GetType {

        @Test
        void 의회식_테이블_타입을_반환한다() {
            ParliamentaryTable parliamentaryTable = new ParliamentaryTable();

            assertThat(parliamentaryTable.getType()).isEqualTo(TableType.PARLIAMENTARY);
        }
    }
}
