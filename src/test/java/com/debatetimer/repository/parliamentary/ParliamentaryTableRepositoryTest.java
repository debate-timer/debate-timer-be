package com.debatetimer.repository.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseRepositoryTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParliamentaryTableRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ParliamentaryTableRepository parliamentaryTableRepository;

    @Nested
    class findAllByMember {

        @Test
        void 특정_회원의_테이블만_조회한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            Member bito = fixtureGenerator.generateMember("비토");
            ParliamentaryTable chanTable1 = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTable chanTable2 = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTable bitoTable = fixtureGenerator.generateParliamentaryTable(bito);

            List<ParliamentaryTable> foundKeoChanTables = parliamentaryTableRepository.findAllByMember(chan);

            assertThat(foundKeoChanTables).containsExactly(chanTable1, chanTable2);
        }
    }

    @Nested
    class findByIdAndMemberId {

        @Test
        void 특정_회원의_테이블을_조회한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);

            Optional<ParliamentaryTable> foundChanTable = parliamentaryTableRepository.findByIdAndMemberId(chanTable.getId(), chan.getId());

            assertAll(
                    () -> assertThat(foundChanTable).isPresent(),
                    () -> assertThat(foundChanTable.get()).usingRecursiveComparison().isEqualTo(chanTable)
            );
        }

        @Test
        void 회원의_테이블이_없으면_조회되지_않는다() {
            Member chan = fixtureGenerator.generateMember("커찬");

            Optional<ParliamentaryTable> foundChanTable = parliamentaryTableRepository.findByIdAndMemberId(1L, chan.getId());

            assertThat(foundChanTable).isNotPresent();
        }
    }
}
