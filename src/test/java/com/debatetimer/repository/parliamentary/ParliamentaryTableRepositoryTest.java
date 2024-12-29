package com.debatetimer.repository.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.BaseRepositoryTest;
import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.controller.exception.errorcode.ClientErrorCode;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParliamentaryTableRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ParliamentaryTableRepository tableRepository;

    @Nested
    class findAllByMember {

        @Test
        void 특정_회원의_테이블만_조회한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            Member bito = fixtureGenerator.generateMember("비토");
            ParliamentaryTable chanTable1 = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTable chanTable2 = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTable bitoTable = fixtureGenerator.generateParliamentaryTable(bito);

            List<ParliamentaryTable> foundKeoChanTables = tableRepository.findAllByMember(chan);

            assertThat(foundKeoChanTables).containsExactly(chanTable1, chanTable2);
        }
    }

    @Nested
    class getOwnerTable {

        @Test
        void 특정_회원의_테이블을_조회한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);

            ParliamentaryTable foundChanTable = tableRepository.getOwnerTable(chanTable.getId(), chan.getId());

            assertThat(foundChanTable).usingRecursiveComparison().isEqualTo(chanTable);
        }

        @Test
        void 회원의_테이블이_없으면_에러를_발생_시킨다() {
            Member chan = fixtureGenerator.generateMember("커찬");

            assertThatThrownBy(() -> tableRepository.getOwnerTable(1L, chan.getId()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.MEMBER_TABLE_NOT_FOUND.getMessage());
        }
    }
}
