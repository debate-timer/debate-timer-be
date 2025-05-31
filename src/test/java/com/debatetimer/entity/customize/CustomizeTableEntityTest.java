package com.debatetimer.entity.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomizeTableEntityTest {

    @Nested
    class GetType {

        @Test
        void 사용자_지정_테이블_타입을_반환한다() {
            CustomizeTableEntity customizeTableEntity = new CustomizeTableEntity();

            assertThat(customizeTableEntity.getType()).isEqualTo(TableType.CUSTOMIZE);
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 테이블의_사용_시각을_업데이트한다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "tableName", "agenda", "찬성", "반대",
                    true, true);
            CustomizeTableEntity customizeTableEntity = new CustomizeTableEntity(table);
            LocalDateTime beforeUsedAt = customizeTableEntity.getUsedAt();
            Thread.sleep(1L);

            customizeTableEntity.updateUsedAt();

            assertThat(customizeTableEntity.getUsedAt()).isAfter(beforeUsedAt);
        }
    }

    @Nested
    class Update {

        @Test
        void 테이블_정보를_업데이트_할_수_있다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "tableName", "agenda", "찬성", "반대",
                    true, true);
            CustomizeTableEntity customizeTableEntity = new CustomizeTableEntity(table);
            CustomizeTable renewTable = new CustomizeTable(member, "newName", "newAgenda",
                    "newPros", "newCons", false, false);
            Thread.sleep(1L);

            customizeTableEntity.updateTable(renewTable);

            assertAll(
                    () -> assertThat(customizeTableEntity.getName()).isEqualTo("newName"),
                    () -> assertThat(customizeTableEntity.getAgenda()).isEqualTo("newAgenda"),
                    () -> assertThat(customizeTableEntity.getProsTeamName()).isEqualTo("newPros"),
                    () -> assertThat(customizeTableEntity.getConsTeamName()).isEqualTo("newCons"),
                    () -> assertThat(customizeTableEntity.isWarningBell()).isFalse(),
                    () -> assertThat(customizeTableEntity.isFinishBell()).isFalse()
            );
        }

        @Test
        void 테이블_업데이트_할_때_사용_시간을_변경한다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "tableName", "agenda", "찬성", "반대",
                    true, true);
            CustomizeTableEntity customizeTableEntity = new CustomizeTableEntity(table);
            CustomizeTable renewTable = new CustomizeTable(member, "newName", "newAgenda",
                    "newPros", "newCons", false, false);
            LocalDateTime beforeUsedAt = customizeTableEntity.getUsedAt();
            Thread.sleep(1L);

            customizeTableEntity.updateTable(renewTable);

            assertThat(customizeTableEntity.getUsedAt()).isAfter(beforeUsedAt);
        }
    }
}
