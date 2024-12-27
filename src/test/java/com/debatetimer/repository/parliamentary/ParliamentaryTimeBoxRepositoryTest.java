package com.debatetimer.repository.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.BaseRepositoryTest;
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
    class findAllByParliamentaryTable {

        @Test
        void 특정_테이블의_타임박스를_모두_조회한다() {
            ParliamentaryTable testTable = fixtureGenerator.generateParliamentaryTable();
            ParliamentaryTimeBox firstBox = fixtureGenerator.generateParliamentaryTimeBox(testTable, 1);
            ParliamentaryTimeBox secondBox = fixtureGenerator.generateParliamentaryTimeBox(testTable, 2);

            List<ParliamentaryTimeBox> foundBoxes = parliamentaryTimeBoxRepository.findAllByParliamentaryTable(testTable);

            assertThat(foundBoxes).containsExactly(firstBox, secondBox);
        }
    }
}
