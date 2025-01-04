package com.debatetimer.repository.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.BaseRepositoryTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
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
            Member chan = fixtureGenerator.generateMember("커찬");
            Member bito = fixtureGenerator.generateMember("비토");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTable bitoTable = fixtureGenerator.generateParliamentaryTable(bito);
            ParliamentaryTimeBox chanBox1 = fixtureGenerator.generateParliamentaryTimeBox(chanTable, 1);
            ParliamentaryTimeBox chanBox2 = fixtureGenerator.generateParliamentaryTimeBox(chanTable, 2);
            ParliamentaryTimeBox bitoBox1 = fixtureGenerator.generateParliamentaryTimeBox(bitoTable, 2);
            ParliamentaryTimeBox bitoBox2 = fixtureGenerator.generateParliamentaryTimeBox(bitoTable, 2);

            List<ParliamentaryTimeBox> foundBoxes = parliamentaryTimeBoxRepository.findAllByParliamentaryTable(
                    chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }
}
