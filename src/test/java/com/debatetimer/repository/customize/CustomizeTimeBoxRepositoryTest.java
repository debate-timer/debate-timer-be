package com.debatetimer.repository.customize;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.repository.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CustomizeTimeBoxRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CustomizeTimeBoxRepository customizeTimeBoxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    class FindAllByCustomizeTable {

        @Test
        void 특정_테이블의_타임박스를_모두_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member bito = memberGenerator.generate("default2@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            CustomizeTable bitoTable = customizeTableGenerator.generate(bito);
            CustomizeTimeBox chanBox1 = customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            CustomizeTimeBox chanBox2 = customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);
            CustomizeTimeBox bitoBox1 = customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);
            CustomizeTimeBox bitoBox2 = customizeTimeBoxGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 2);

            List<CustomizeTimeBox> foundBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(chanTable);

            assertThat(foundBoxes).containsExactly(chanBox1, chanBox2);
        }
    }

    //TODO : 에러 원인 파악하기
//    @Nested
//    class SaveAll {
//
//        @Test
//        void 타임박스를_모두_저장한다() {
//            Member chan = memberGenerator.generate("default@gmail.com");
//            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
//            CustomizeTimeBox chanBox1 = new CustomizeTimeBox(chanTable, 1, Stance.PROS, "순서1", CustomizeBoxType.NORMAL,
//                    60, "커찬");
//            CustomizeTimeBox chanBox2 = new CustomizeTimeBox(chanTable, 2, Stance.PROS, "순서2", CustomizeBoxType.NORMAL,
//                    60, "커찬");
//
//            customizeTimeBoxRepository.saveAll(List.of(chanBox1, chanBox2));
//
//            List<CustomizeTimeBox> timeBoxes = jdbcTemplate.queryForList(
//                    "select * from customize_time_box where table_id = ?",
//                    new Object[]{chanTable.getId()},
//                    CustomizeTimeBox.class
//            );
//            assertThat(timeBoxes).hasSize(2);
//        }
//    }
}
