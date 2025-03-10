package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.dto.member.TableType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomizeTableTest {

    @Nested
    class GetType {

        @Test
        void 사용자_지정_테이블_타입을_반환한다() {
            CustomizeTable customizeTable = new CustomizeTable();

            assertThat(customizeTable.getType()).isEqualTo(TableType.PARLIAMENTARY);
        }
    }
}
