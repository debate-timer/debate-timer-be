package com.debatetimer.service.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseServiceTest;
import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParliamentaryServiceTest extends BaseServiceTest {

    @Autowired
    ParliamentaryService parliamentaryService;

    @Nested
    class save {

        @Test
        void 의회식_토론_테이블을_생성_한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            ParliamentaryTableCreateRequest chanTableRequest =  dtoGenerator.generateParliamentaryTableCreateRequest("커찬의 토론 테이블");
            TableInfoCreateRequest requestTableInfo = chanTableRequest.info();
            List<TimeBoxCreateRequest> requestTimeBoxes = chanTableRequest.table();

            ParliamentaryTableResponse savedTableResponse = parliamentaryService.save(chanTableRequest, chan);
            Optional<ParliamentaryTable> foundTable = parliamentaryTableRepository.findById(savedTableResponse.id());
            List<ParliamentaryTimeBox> foundTimeBoxes = timeBoxRepository.findAllByParliamentaryTable(foundTable.get());

            assertAll(
                    () -> assertThat(foundTable.get().getName()).isEqualTo(requestTableInfo.name()),
                    () -> assertThat(foundTimeBoxes).hasSize(requestTimeBoxes.size())
            );
        }
    }

    @Nested
    class findTable {

        @Test
        void 의회식_토론_테이블을_조회_한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);
            fixtureGenerator.generateParliamentaryTimeBox(chanTable, 1);
            fixtureGenerator.generateParliamentaryTimeBox(chanTable, 2);

            ParliamentaryTableResponse foundResponse = parliamentaryService.findTable(chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(foundResponse.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(foundResponse.table()).hasSize(2)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_조회_시_예외_발생() {
            Member chan = fixtureGenerator.generateMember("커찬");
            Member coli = fixtureGenerator.generateMember("콜리");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);

            assertThatThrownBy(() -> parliamentaryService.findTable(chanTable.getId(), coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage("회원님의 토론 테이블을 찾을 수 없습니다.");
        }
    }

    @Nested
    class updateTable {

        @Test
        void 의회식_토론_테이블을_수정_한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTableCreateRequest renewTableRequest =  dtoGenerator.generateParliamentaryTableCreateRequest("새로운 테이블");
            TableInfoCreateRequest renewTableInfo = renewTableRequest.info();
            List<TimeBoxCreateRequest> renewTimeBoxes = renewTableRequest.table();

            ParliamentaryTableResponse updatedTable = parliamentaryService.updateTable(renewTableRequest, chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(updatedTable.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(updatedTable.info().name()).isEqualTo(renewTableInfo.name()),
                    () -> assertThat(updatedTable.table()).hasSize(renewTimeBoxes.size())
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_수정_시_예외_발생() {
            Member chan = fixtureGenerator.generateMember("커찬");
            Member coli = fixtureGenerator.generateMember("콜리");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);
            ParliamentaryTableCreateRequest renewTableRequest =  dtoGenerator.generateParliamentaryTableCreateRequest("새로운 테이블");

            assertThatThrownBy(() -> parliamentaryService.updateTable(renewTableRequest, chanTable.getId(), coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage("회원님의 토론 테이블을 찾을 수 없습니다.");
        }
    }

    @Nested
    class deleteTable {

        @Test
        void 의회식_토론_테이블을_삭제_한다() {
            Member chan = fixtureGenerator.generateMember("커찬");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);
            fixtureGenerator.generateParliamentaryTimeBox(chanTable, 1);
            fixtureGenerator.generateParliamentaryTimeBox(chanTable, 2);

            parliamentaryService.deleteTable(chanTable.getId(), chan);

            Optional<ParliamentaryTable> foundTable = parliamentaryTableRepository.findById(chanTable.getId());
            List<ParliamentaryTimeBox> timeBoxes = timeBoxRepository.findAllByParliamentaryTable(chanTable);

            assertAll(
                    () -> assertThat(foundTable).isNotPresent(),
                    () -> assertThat(timeBoxes).isEmpty()
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_삭제_시_예외_발생() {
            Member chan = fixtureGenerator.generateMember("커찬");
            Member coli = fixtureGenerator.generateMember("콜리");
            ParliamentaryTable chanTable = fixtureGenerator.generateParliamentaryTable(chan);

            assertThatThrownBy(() -> parliamentaryService.deleteTable(chanTable.getId(), coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage("회원님의 토론 테이블을 찾을 수 없습니다.");
        }
    }
}
