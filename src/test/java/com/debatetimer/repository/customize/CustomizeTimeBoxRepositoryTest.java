package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBox;
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
            CustomizeTableEntity chanTable = customizeTableGenerator.generate(chan);
            CustomizeTableEntity bitoTable = customizeTableGenerator.generate(bito);
            CustomizeTimeBox chanBox1 = customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBox chanBox2 = customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);

            List<CustomizeTimeBox> foundBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }
}
