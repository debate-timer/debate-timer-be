package com.debatetimer.repository.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParliamentaryTableRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ParliamentaryTableRepository tableRepository;

    @Nested
    class FindAllByMember {

        @Test
        void 특정_회원의_테이블만_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            ParliamentaryTable chanTable1 = tableGenerator.generate(chan);
            ParliamentaryTable chanTable2 = tableGenerator.generate(chan);
            ParliamentaryTable bitoTable = tableGenerator.generate(bito);

            List<ParliamentaryTable> foundKeoChanTables = tableRepository.findAllByMember(chan);

            assertThat(foundKeoChanTables).containsExactly(chanTable1, chanTable2);
        }
    }

    @Nested
    class GetById {

        @Test
        void 특정_아이디의_테이블을_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);

            ParliamentaryTable foundChanTable = tableRepository.getById(chanTable.getId());

            assertThat(foundChanTable).usingRecursiveComparison().isEqualTo(chanTable);
        }

        @Test
        void 특정_아이디의_테이블이_없으면_에러를_발생시킨다() {
            assertThatThrownBy(() -> tableRepository.getById(1L))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }
}
