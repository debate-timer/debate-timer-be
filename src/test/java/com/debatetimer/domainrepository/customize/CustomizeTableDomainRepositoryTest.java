package com.debatetimer.domainrepository.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.Bell;
import com.debatetimer.domain.customize.BellType;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.customize.BellEntity;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomizeTableDomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private CustomizeTableDomainRepository customizeTableDomainRepository;

    @Nested
    class Save {

        @Test
        void 테이블을_저장한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTable table = tableGenerator.generate(member);
            List<Bell> bells = bellGenerator.generate(3);
            CustomizeTimeBox timeBox1 = timeBoxGenerator.generate(bells);
            CustomizeTimeBox timeBox2 = timeBoxGenerator.generate(Collections.emptyList());

            CustomizeTable savedTable = customizeTableDomainRepository.save(table, List.of(timeBox1, timeBox2));
            List<CustomizeTimeBoxEntity> timeBoxEntities = timeBoxRepository.findAllByCustomizeTable(
                    new CustomizeTableEntity(savedTable));
            List<BellEntity> bellEntities = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntities);

            assertAll(
                    () -> assertThat(savedTable.getName()).isEqualTo(table.getName()),
                    () -> assertThat(timeBoxEntities).hasSize(2),
                    () -> assertThat(bellEntities).hasSize(3)
            );
        }
    }

    @Nested
    class GetByIdAndMember {

        @Test
        void 회원이_소유한_테이블을_가져온다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = tableEntityGenerator.generate(member);

            CustomizeTable foundTable = customizeTableDomainRepository.getByIdAndMember(table.getId(), member);

            assertThat(foundTable.getId()).isEqualTo(table.getId());
        }
    }

    @Nested
    class GetCustomizeTimeBoxes {

        @Test
        void 테이블의_시간박스는_순서대로_가져온다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity tableEntity = tableEntityGenerator.generate(member);
            timeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 1, 60);
            timeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 2, 180);
            timeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 3, 120);

            List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getMemberCustomizeTimeBoxes(
                    tableEntity.getId(), member);

            assertThat(timeBoxes).hasSize(3)
                    .extracting(CustomizeTimeBox::getTime)
                    .containsExactly(60, 180, 120);
        }

        @Test
        void 테이블의_시간박스와_벨을_가져온다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity tableEntity = tableEntityGenerator.generate(member);
            CustomizeTimeBoxEntity timeBoxEntity1 = timeBoxEntityGenerator.generate(tableEntity,
                    CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBoxEntity timeBoxEntity2 = timeBoxEntityGenerator.generate(tableEntity,
                    CustomizeBoxType.NORMAL, 2);
            bellEntityGenerator.generate(timeBoxEntity1, BellType.BEFORE_END, 20, 1);
            bellEntityGenerator.generate(timeBoxEntity1, BellType.BEFORE_END, 30, 1);
            bellEntityGenerator.generate(timeBoxEntity2, BellType.BEFORE_END, 10, 1);

            List<CustomizeTimeBox> timeBoxes = customizeTableDomainRepository.getMemberCustomizeTimeBoxes(
                    tableEntity.getId(), member);

            assertAll(
                    () -> assertThat(timeBoxes).hasSize(2),
                    () -> assertThat(timeBoxes.get(0).getBells()).hasSize(2)
                            .extracting(Bell::getTime)
                            .containsExactly(20, 30),
                    () -> assertThat(timeBoxes.get(1).getBells()).hasSize(1)
                            .extracting(Bell::getTime)
                            .containsExactlyInAnyOrder(10)
            );
        }
    }

    @Nested
    class Update {

        @Test
        void 테이블을_수정한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity tableEntity = tableEntityGenerator.generate(member);
            CustomizeTimeBoxEntity timeBox1 = timeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBoxEntity timeBox2 = timeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 2);
            bellEntityGenerator.generate(timeBox1, BellType.AFTER_START, 10, 1);
            bellEntityGenerator.generate(timeBox1, BellType.AFTER_START, 20, 1);
            bellEntityGenerator.generate(timeBox2, BellType.AFTER_START, 20, 1);

            CustomizeTable table = tableGenerator.generate(member, "수정된 테이블");
            List<Bell> bells = bellGenerator.generate(3);
            CustomizeTimeBox timeBox = timeBoxGenerator.generate(bells, "수정");

            CustomizeTable updatedTable = customizeTableDomainRepository.update(table, tableEntity.getId(), member,
                    List.of(timeBox));
            CustomizeTableEntity foundTable = tableRepository.getByIdAndMember(tableEntity.getId(), member);
            List<CustomizeTimeBoxEntity> timeBoxEntities = timeBoxRepository.findAllByCustomizeTable(
                    new CustomizeTableEntity(updatedTable));
            List<BellEntity> bellEntities = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntities);

            assertAll(
                    () -> assertThat(foundTable.getName()).isEqualTo(updatedTable.getName()),
                    () -> assertThat(timeBoxEntities).hasSize(1),
                    () -> assertThat(bellEntities).hasSize(3)
            );
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 테이블의_사용_시간을_업데이트한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity tableEntity = tableEntityGenerator.generate(member);
            LocalDateTime beforeUsedAt = tableEntity.getUsedAt();

            CustomizeTable customizeTable = customizeTableDomainRepository.updateUsedAt(tableEntity.getId(), member);

            assertAll(
                    () -> assertThat(customizeTable.getId()).isEqualTo(tableEntity.getId()),
                    () -> assertThat(customizeTable.getUsedAt()).isAfter(beforeUsedAt)
            );
        }
    }

    @Nested
    class Delete {

        @Test
        void 테이블을_삭제한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = tableEntityGenerator.generate(member);
            CustomizeTimeBoxEntity timeBox1 = timeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBoxEntity timeBox2 = timeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 2);
            bellEntityGenerator.generate(timeBox1, BellType.AFTER_START, 10, 1);
            bellEntityGenerator.generate(timeBox1, BellType.AFTER_START, 20, 1);
            bellEntityGenerator.generate(timeBox2, BellType.AFTER_START, 20, 1);

            customizeTableDomainRepository.delete(table.getId(), member);
            Optional<CustomizeTableEntity> foundTable = tableRepository.findById(table.getId());
            List<CustomizeTimeBoxEntity> timeBoxEntities = timeBoxRepository.findAllByCustomizeTable(table);
            List<BellEntity> bellEntities = bellRepository.findAllByCustomizeTimeBoxIn(timeBoxEntities);

            assertAll(
                    () -> assertThat(foundTable).isEmpty(),
                    () -> assertThat(timeBoxEntities).isEmpty(),
                    () -> assertThat(bellEntities).isEmpty()
            );
        }
    }
}
