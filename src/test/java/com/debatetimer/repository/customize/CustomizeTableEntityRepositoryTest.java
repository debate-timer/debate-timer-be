package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomizeTableEntityRepositoryTest extends BaseRepositoryTest {

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
}
