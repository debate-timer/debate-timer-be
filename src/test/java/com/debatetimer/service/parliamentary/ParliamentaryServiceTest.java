package com.debatetimer.service.parliamentary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTimeBoxCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
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
            Member chan = memberGenerator.generate("default@gmail.com");
            ParliamentaryTableCreateRequest chanTableRequest = new ParliamentaryTableCreateRequest(
                    new ParliamentaryTableInfoCreateRequest("커찬의 테이블", "주제", true, true),
                    List.of(new ParliamentaryTimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new ParliamentaryTimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)));

            ParliamentaryTableResponse savedTableResponse = parliamentaryService.save(chanTableRequest, chan);
            Optional<ParliamentaryTable> foundTable = parliamentaryTableRepository.findById(savedTableResponse.id());
            List<ParliamentaryTimeBox> foundTimeBoxes = parliamentaryTimeBoxRepository.findAllByParliamentaryTable(
                    foundTable.get());

            assertAll(
                    () -> assertThat(foundTable.get().getName()).isEqualTo(chanTableRequest.info().name()),
                    () -> assertThat(foundTimeBoxes).hasSize(chanTableRequest.table().size())
            );
        }
    }

    @Nested
    class FindTable {

        @Test
        void 의회식_토론_테이블을_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
            parliamentaryTimeBoxGenerator.generate(chanTable, 1);
            parliamentaryTimeBoxGenerator.generate(chanTable, 2);

            ParliamentaryTableResponse foundResponse = parliamentaryService.findTable(chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(foundResponse.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(foundResponse.table()).hasSize(2)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_조회_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
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
            Member chan = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
            ParliamentaryTableCreateRequest renewTableRequest = new ParliamentaryTableCreateRequest(
                    new ParliamentaryTableInfoCreateRequest("커찬의 테이블", "주제", true, true),
                    List.of(new ParliamentaryTimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new ParliamentaryTimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)));

            parliamentaryService.updateTable(renewTableRequest, chanTable.getId(), chan);

            Optional<ParliamentaryTable> updatedTable = parliamentaryTableRepository.findById(chanTable.getId());
            List<ParliamentaryTimeBox> updatedTimeBoxes = parliamentaryTimeBoxRepository.findAllByParliamentaryTable(
                    updatedTable.get());

            assertAll(
                    () -> assertThat(updatedTable.get().getId()).isEqualTo(chanTable.getId()),
                    () -> assertThat(updatedTable.get().getName()).isEqualTo(renewTableRequest.info().name()),
                    () -> assertThat(updatedTimeBoxes).hasSize(renewTableRequest.table().size())
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_수정_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();
            ParliamentaryTableCreateRequest renewTableRequest = new ParliamentaryTableCreateRequest(
                    new ParliamentaryTableInfoCreateRequest("새로운 테이블", "주제", true, true),
                    List.of(new ParliamentaryTimeBoxCreateRequest(Stance.PROS, ParliamentaryBoxType.OPENING, 3, 1),
                            new ParliamentaryTimeBoxCreateRequest(Stance.CONS, ParliamentaryBoxType.OPENING, 3, 1)));

            assertThatThrownBy(() -> parliamentaryService.updateTable(renewTableRequest, chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 의회식_토론_테이블의_사용_시각을_최신화한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable table = parliamentaryTableGenerator.generate(member);
            LocalDateTime beforeUsedAt = table.getUsedAt();

            parliamentaryService.updateUsedAt(table.getId(), member);

            Optional<ParliamentaryTable> updatedTable = parliamentaryTableRepository.findById(table.getId());

            assertAll(
                    () -> assertThat(updatedTable.get().getId()).isEqualTo(table.getId()),
                    () -> assertThat(updatedTable.get().getUsedAt()).isAfter(beforeUsedAt)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_수정_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> parliamentaryService.updateUsedAt(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 의회식_토론_테이블을_삭제한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
            parliamentaryTimeBoxGenerator.generate(chanTable, 1);
            parliamentaryTimeBoxGenerator.generate(chanTable, 2);

            parliamentaryService.deleteTable(chanTable.getId(), chan);

            Optional<ParliamentaryTable> foundTable = parliamentaryTableRepository.findById(chanTable.getId());
            List<ParliamentaryTimeBox> timeBoxes = parliamentaryTimeBoxRepository.findAllByParliamentaryTable(
                    chanTable);

            assertAll(
                    () -> assertThat(foundTable).isEmpty(),
                    () -> assertThat(timeBoxes).isEmpty()
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_삭제_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            ParliamentaryTable chanTable = parliamentaryTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> parliamentaryService.deleteTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }
}
