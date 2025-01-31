package com.debatetimer.repository.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParliamentaryTimeBoxRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ParliamentaryTimeBoxRepository parliamentaryTimeBoxRepository;

    @Nested
    class FindAllByParliamentaryTable {

        @Test
        void 특정_테이블의_타임박스를_모두_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            ParliamentaryTable bitoTable = tableGenerator.generate(bito);
            ParliamentaryTimeBox chanBox1 = timeBoxGenerator.generate(chanTable, 1);
            ParliamentaryTimeBox chanBox2 = timeBoxGenerator.generate(chanTable, 2);
            ParliamentaryTimeBox bitoBox1 = timeBoxGenerator.generate(bitoTable, 2);
            ParliamentaryTimeBox bitoBox2 = timeBoxGenerator.generate(bitoTable, 2);

            List<ParliamentaryTimeBox> foundBoxes = parliamentaryTimeBoxRepository.findAllByParliamentaryTable(
                    chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }
}
