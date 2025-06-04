package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomizeTableRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CustomizeTableRepository tableRepository;

    @Nested
    class FindAllByMember {

        @Test
        void 특정_회원의_테이블만_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            CustomizeTableEntity chanTable1 = customizeTableGenerator.generate(chan);
            CustomizeTableEntity chanTable2 = customizeTableGenerator.generate(chan);
            customizeTableGenerator.generate(bito);

            List<CustomizeTableEntity> foundKeoChanTables = tableRepository.findAllByMember(chan);

            assertThat(foundKeoChanTables).containsExactly(chanTable1, chanTable2);
        }
    }

    @Nested
    class GetByIdAndMember {

        @Test
        void 특정_회원의_테이블을_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity table = customizeTableGenerator.generate(chan);

            CustomizeTableEntity foundTable = tableRepository.getByIdAndMember(table.getId(), chan);

            assertThat(foundTable).isEqualTo(table);
        }

        @Test
        void 존재하지_않는_테이블을_조회하면_예외를_던진다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            customizeTableGenerator.generate(chan);
            long nonExistTableId = 99999999L;

            assertThatThrownBy(() -> tableRepository.getByIdAndMember(nonExistTableId, chan))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessageContaining(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }
}
