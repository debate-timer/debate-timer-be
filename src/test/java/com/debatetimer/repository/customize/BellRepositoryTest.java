package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BellRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BellRepository bellRepository;

    @Nested
    class DeleteAllByTable {

        @Test
        void 특정_테이블에_해당하는_벨을_삭제한다() {
            Member member = memberGenerator.generate("chan@gmail.com");
            CustomizeTableEntity deleteBellTable = customizeTableGenerator.generate(member);
            CustomizeTableEntity otherTable = customizeTableGenerator.generate(member);
            CustomizeTimeBoxEntity deleteBellTimeBox = customizeTimeBoxGenerator.generate(deleteBellTable,
                    CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBoxEntity otherTimeBox = customizeTimeBoxGenerator.generate(otherTable,
                    CustomizeBoxType.NORMAL, 1);
            bellGenerator.generate(deleteBellTimeBox, 45, 1);
            bellGenerator.generate(deleteBellTimeBox, 60, 1);
            bellGenerator.generate(otherTimeBox, 45, 1);

            bellRepository.deleteAllByTable(deleteBellTable.getId());

            assertAll(
                    () -> assertThat(bellRepository.findAllByCustomizeTimeBoxIn(List.of(deleteBellTimeBox))).isEmpty(),
                    () -> assertThat(bellRepository.findAllByCustomizeTimeBoxIn(List.of(otherTimeBox))).hasSize(1)
            );

        }
    }

}
