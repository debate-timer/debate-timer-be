package com.debatetimer.service.timebased;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.timebased.TimeBasedBoxType;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.domain.timebased.TimeBasedTimeBox;
import com.debatetimer.dto.timebased.request.TimeBasedTableCreateRequest;
import com.debatetimer.dto.timebased.request.TimeBasedTableInfoCreateRequest;
import com.debatetimer.dto.timebased.request.TimeBasedTimeBoxCreateRequest;
import com.debatetimer.dto.timebased.response.TimeBasedTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TimeBasedServiceTest extends BaseServiceTest {

    @Autowired
    private TimeBasedService timeBasedService;

    @Nested
    class Save {

        @Test
        void 시간총량제_토론_테이블을_생성한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            TimeBasedTableInfoCreateRequest requestTableInfo = new TimeBasedTableInfoCreateRequest("커찬의 테이블",
                    "주제", true, true);
            List<TimeBasedTimeBoxCreateRequest> requestTimeBoxes = List.of(
                    new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                            1),
                    new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                            60,
                            1));
            TimeBasedTableCreateRequest chanTableRequest = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("커찬의 테이블", "주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));

            TimeBasedTableResponse savedTableResponse = timeBasedService.save(chanTableRequest, chan);
            Optional<TimeBasedTable> foundTable = timeBasedTableRepository.findById(savedTableResponse.id());
            List<TimeBasedTimeBox> foundTimeBoxes = timeBasedTimeBoxRepository.findAllByTimeBasedTable(
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
        void 시간총량제_토론_테이블을_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            timeBasedTimeBoxGenerator.generate(chanTable, 1);
            timeBasedTimeBoxGenerator.generate(chanTable, 2);

            TimeBasedTableResponse foundResponse = timeBasedService.findTable(chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(foundResponse.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(foundResponse.table()).hasSize(2)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_조회_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> timeBasedService.findTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class UpdateTable {

        @Test
        void 시간총량제_토론_테이블을_수정한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            TimeBasedTableCreateRequest renewTableRequest = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("커찬의 테이블", "주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));

            timeBasedService.updateTable(renewTableRequest, chanTable.getId(), chan);

            Optional<TimeBasedTable> updatedTable = timeBasedTableRepository.findById(chanTable.getId());
            List<TimeBasedTimeBox> updatedTimeBoxes = timeBasedTimeBoxRepository.findAllByTimeBasedTable(
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
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();
            TimeBasedTableCreateRequest renewTableRequest = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("새로운 테이블", "주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));

            assertThatThrownBy(() -> timeBasedService.updateTable(renewTableRequest, chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 시간총량제_토론_테이블을_수정한다() throws InterruptedException {
            Member member = memberGenerator.generate("default@gmail.com");
            TimeBasedTable table = timeBasedTableGenerator.generate(member);
            LocalDateTime beforeUsedAt = table.getUsedAt();
            Thread.sleep(1);

            timeBasedService.updateUsedAt(table.getId(), member);

            Optional<TimeBasedTable> updatedTable = timeBasedTableRepository.findById(table.getId());
            assertAll(
                    () -> assertThat(updatedTable.get().getId()).isEqualTo(table.getId()),
                    () -> assertThat(updatedTable.get().getUsedAt()).isAfter(beforeUsedAt)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_수정_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();
            TimeBasedTableCreateRequest renewTableRequest = new TimeBasedTableCreateRequest(
                    new TimeBasedTableInfoCreateRequest("새로운 테이블", "주제", true, true),
                    List.of(new TimeBasedTimeBoxCreateRequest(Stance.PROS, TimeBasedBoxType.OPENING, 120, null, null,
                                    1),
                            new TimeBasedTimeBoxCreateRequest(Stance.NEUTRAL, TimeBasedBoxType.TIME_BASED, 360, 180,
                                    60,
                                    1)));

            assertThatThrownBy(() -> timeBasedService.updateTable(renewTableRequest, chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 시간총량제_토론_테이블을_삭제한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            timeBasedTimeBoxGenerator.generate(chanTable, 1);
            timeBasedTimeBoxGenerator.generate(chanTable, 2);

            timeBasedService.deleteTable(chanTable.getId(), chan);

            Optional<TimeBasedTable> foundTable = timeBasedTableRepository.findById(chanTable.getId());
            List<TimeBasedTimeBox> timeBoxes = timeBasedTimeBoxRepository.findAllByTimeBasedTable(
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
            TimeBasedTable chanTable = timeBasedTableGenerator.generate(chan);
            Long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> timeBasedService.deleteTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.NOT_TABLE_OWNER.getMessage());
        }
    }
}
