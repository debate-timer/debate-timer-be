package com.debatetimer.repository.time_based;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TimeBasedTimeBoxRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TimeBasedTimeBoxRepository timeBasedTimeBoxRepository;

    @Nested
    class FindAllByParliamentaryTable {

        @Test
        void 특정_테이블의_타임박스를_모두_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            TimeBasedTable bitoTable = timeBasedTableGenerator.generate(bito);
            TimeBasedTimeBox chanBox1 = timeBasedTimeBoxGenerator.generate(chanTable, 1);
            TimeBasedTimeBox chanBox2 = timeBasedTimeBoxGenerator.generate(chanTable, 2);
            TimeBasedTimeBox bitoBox1 = timeBasedTimeBoxGenerator.generate(bitoTable, 2);
            TimeBasedTimeBox bitoBox2 = timeBasedTimeBoxGenerator.generate(bitoTable, 2);

            List<TimeBasedTimeBox> foundBoxes = timeBasedTimeBoxRepository.findAllByTimeBasedTable(
                    chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }
}