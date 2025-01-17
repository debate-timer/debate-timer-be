package com.debatetimer.service.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParliamentaryServiceTest extends BaseServiceTest {

    @Autowired
    private ParliamentaryService parliamentaryService;

    @Nested
    class Save {

        @Test
        void 의회식_토론_테이블을_생성한다() {
            Member chan = memberGenerator.generate("커찬");
            TableInfoCreateRequest requestTableInfo = new TableInfoCreateRequest("커찬의 테이블", "주제");
            List<TimeBoxCreateRequest> requestTimeBoxes = List.of(
                    new TimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 3, 1),
                    new TimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 3, 1)
            );
            ParliamentaryTableCreateRequest chanTableRequest = new ParliamentaryTableCreateRequest(
                    requestTableInfo,
                    requestTimeBoxes
            );

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
    class FindTable {

        @Test
        void 의회식_토론_테이블을_조회한다() {
            Member chan = memberGenerator.generate("커찬");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            timeBoxGenerator.generate(chanTable, 1);
            timeBoxGenerator.generate(chanTable, 2);

            ParliamentaryTableResponse foundResponse = parliamentaryService.findTable(chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(foundResponse.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(foundResponse.table()).hasSize(2)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_조회_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("커찬");
            Member coli = memberGenerator.generate("콜리");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> parliamentaryService.findTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class UpdateTable {

        @Test
        void 의회식_토론_테이블을_수정한다() {
            Member chan = memberGenerator.generate("커찬");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            TableInfoCreateRequest renewTableInfo = new TableInfoCreateRequest("커찬 테이블", "주제");
            List<TimeBoxCreateRequest> renewTimeBoxes = List.of(
                    new TimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 3, 1),
                    new TimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 3, 1)
            );
            ParliamentaryTableCreateRequest renewTableRequest = new ParliamentaryTableCreateRequest(
                    renewTableInfo,
                    renewTimeBoxes
            );

            ParliamentaryTableResponse updatedTable = parliamentaryService.updateTable(renewTableRequest,
                    chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(updatedTable.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(updatedTable.info().name()).isEqualTo(renewTableInfo.name()),
                    () -> assertThat(updatedTable.table()).hasSize(renewTimeBoxes.size())
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_수정_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("커찬");
            Member coli = memberGenerator.generate("콜리");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            long chanTableId = chanTable.getId();
            TableInfoCreateRequest renewTableInfo = new TableInfoCreateRequest("새로운 테이블", "주제");
            List<TimeBoxCreateRequest> renewTimeBoxes = List.of(
                    new TimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 3, 1),
                    new TimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 3, 1)
            );
            ParliamentaryTableCreateRequest renewTableRequest = new ParliamentaryTableCreateRequest(
                    renewTableInfo,
                    renewTimeBoxes
            );

            assertThatThrownBy(() -> parliamentaryService.updateTable(renewTableRequest, chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 의회식_토론_테이블을_삭제한다() {
            Member chan = memberGenerator.generate("커찬");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            timeBoxGenerator.generate(chanTable, 1);
            timeBoxGenerator.generate(chanTable, 2);

            parliamentaryService.deleteTable(chanTable.getId(), chan);

            Optional<ParliamentaryTable> foundTable = parliamentaryTableRepository.findById(chanTable.getId());
            List<ParliamentaryTimeBox> timeBoxes = timeBoxRepository.findAllByParliamentaryTable(chanTable);

            assertAll(
                    () -> assertThat(foundTable).isEmpty(),
                    () -> assertThat(timeBoxes).isEmpty()
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_삭제_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("커찬");
            Member coli = memberGenerator.generate("콜리");
            ParliamentaryTable chanTable = tableGenerator.generate(chan);
            Long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> parliamentaryService.deleteTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }
}
