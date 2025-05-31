package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomizeTimeBoxRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CustomizeTimeBoxRepository customizeTimeBoxRepository;

    @Nested
    class FindAllByCustomizeTable {

        @Test
        void 특정_테이블의_타임박스를_모두_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            CustomizeTimeBox chanBox1 = customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBox chanBox2 = customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);

            List<CustomizeTimeBox> foundBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }

    @Nested
    class DeleteAllByTable {

        @Test
        void 특정_테이블의_타임박스를_모두_삭제한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);

            customizeTimeBoxRepository.deleteAllByTable(chanTable);

            List<CustomizeTimeBox> timeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(chanTable);
            assertThat(timeBoxes).isEmpty();
        }

        @Test
        void 특정_테이블의_타임_박스를_삭제해도_다른_테이블의_타임_박스는_삭제되지_않는다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTable filledTable = customizeTableGenerator.generate(chan);
            customizeTimeBoxGenerator.generate(filledTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(filledTable, CustomizeBoxType.NORMAL, 2);
            CustomizeTable deletedTable = customizeTableGenerator.generate(chan);
            customizeTimeBoxGenerator.generate(deletedTable, CustomizeBoxType.NORMAL, 1);

            customizeTimeBoxRepository.deleteAllByTable(deletedTable);

            List<CustomizeTimeBox> timeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(filledTable);
            assertThat(timeBoxes).hasSize(2);
        }

        @Test
        void 테이블의_타임_박스가_없을_경우_타임_박스_삭제_시_예외가_발생하지_않는다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTable emptyTable = customizeTableGenerator.generate(chan);

            assertThatCode(() -> customizeTimeBoxRepository.deleteAllByTable(emptyTable))
                    .doesNotThrowAnyException();
        }
    }
}
