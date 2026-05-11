package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomizeTimeBoxRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CustomizeTimeBoxRepository customizeTimeBoxRepository;

    @Nested
    class FindAllByCustomizeTableEntity {

        @Test
        void 특정_테이블의_타임박스를_모두_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            CustomizeTableEntity chanTable = customizeTableEntityGenerator.generate(chan);
            CustomizeTableEntity bitoTable = customizeTableEntityGenerator.generate(bito);
            CustomizeTimeBoxEntity chanBox1 = customizeTimeBoxEntityGenerator.generate(chanTable,
                    CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBoxEntity chanBox2 = customizeTimeBoxEntityGenerator.generate(chanTable,
                    CustomizeBoxType.NORMAL, 2);
            customizeTimeBoxEntityGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            customizeTimeBoxEntityGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);

            List<CustomizeTimeBoxEntity> foundBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }

    @Nested
    class DeleteAllByTable {

        @Test
        void 특정_테이블의_타임박스를_모두_삭제한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity chanTable = customizeTableEntityGenerator.generate(chan);
            customizeTimeBoxEntityGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxEntityGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);

            customizeTimeBoxRepository.deleteAllByTable(chanTable.getId());

            List<CustomizeTimeBoxEntity> timeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(chanTable);
            assertThat(timeBoxes).isEmpty();
        }

        @Test
        void 특정_테이블의_타임_박스를_삭제해도_다른_테이블의_타임_박스는_삭제되지_않는다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity filledTable = customizeTableEntityGenerator.generate(chan);
            customizeTimeBoxEntityGenerator.generate(filledTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxEntityGenerator.generate(filledTable, CustomizeBoxType.NORMAL, 2);
            CustomizeTableEntity deletedTable = customizeTableEntityGenerator.generate(chan);
            customizeTimeBoxEntityGenerator.generate(deletedTable, CustomizeBoxType.NORMAL, 1);

            customizeTimeBoxRepository.deleteAllByTable(deletedTable.getId());

            List<CustomizeTimeBoxEntity> timeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(filledTable);
            assertThat(timeBoxes).hasSize(2);
        }

        @Test
        void 테이블의_타임_박스가_없을_경우_타임_박스_삭제_시_예외가_발생하지_않는다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity emptyTable = customizeTableEntityGenerator.generate(chan);

            assertThatCode(() -> customizeTimeBoxRepository.deleteAllByTable(emptyTable.getId()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class SumTimeByTableId {

        @Test
        void 특정_테이블의_타임_박스시간의_합을_반환한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity debateTable = customizeTableEntityGenerator.generate(chan);
            CustomizeTimeBoxEntity timeBox1 = customizeTimeBoxEntityGenerator.generate(debateTable,
                    CustomizeBoxType.NORMAL, 1, 10);
            CustomizeTimeBoxEntity timeBox2 = customizeTimeBoxEntityGenerator.generate(debateTable,
                    CustomizeBoxType.NORMAL, 2, 20);

            long summedTimeByTableId = customizeTimeBoxRepository.sumTimeByTableId(debateTable.getId());

            assertThat(summedTimeByTableId).isEqualTo(timeBox1.getTime() + timeBox2.getTime());
        }
    }
}
