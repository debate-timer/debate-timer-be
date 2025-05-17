package com.debatetimer.service.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.customize.CustomizeTimeBox;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTableInfoCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomizeServiceTest extends BaseServiceTest {

    @Autowired
    private CustomizeService customizeService;

    @Nested
    class Save {

        @Test
        void 사용자_지정_토론_테이블을_생성한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTableCreateRequest customizeTableCreateRequest = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );

            CustomizeTableResponse savedTableResponse = customizeService.save(customizeTableCreateRequest, chan);
            Optional<CustomizeTable> foundTable = customizeTableRepository.findById(savedTableResponse.id());
            List<CustomizeTimeBox> foundTimeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(
                    foundTable.get());

            assertAll(
                    () -> assertThat(foundTable.get().getName()).isEqualTo(customizeTableCreateRequest.info().name()),
                    () -> assertThat(foundTimeBoxes).hasSize(customizeTableCreateRequest.table().size())
            );
        }
    }

    @Nested
    class FindTable {

        @Test
        void 사용자_지정_토론_테이블을_조회한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);

            CustomizeTableResponse foundResponse = customizeService.findTable(chanTable.getId(), chan);

            assertAll(
                    () -> assertThat(foundResponse.id()).isEqualTo(chanTable.getId()),
                    () -> assertThat(foundResponse.table()).hasSize(2)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_조회_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> customizeService.findTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class UpdateTable {

        @Test
        void 사용자_지정_토론_테이블을_수정한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            CustomizeTableCreateRequest renewTableRequest = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );

            customizeService.updateTable(renewTableRequest, chanTable.getId(), chan);

            Optional<CustomizeTable> updatedTable = customizeTableRepository.findById(chanTable.getId());
            List<CustomizeTimeBox> updatedTimeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(
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
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();
            CustomizeTableCreateRequest renewTableRequest = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );

            assertThatThrownBy(() -> customizeService.updateTable(renewTableRequest, chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }

        @Test
        void 테이블_정보_수정을_동시에_요청할_때_동시에_처리하지_않는다() throws InterruptedException {
            Member member = memberGenerator.generate("default@gmail.com");
            CustomizeTable table = customizeTableGenerator.generate(member);
            CustomizeTableCreateRequest request = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );

            runAtSameTime(2, () -> customizeService.updateTable(request, table.getId(), member));

            assertThat(customizeTimeBoxRepository.findAllByCustomizeTable(table)).hasSize(2);
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 사용자_지정_토론_테이블의_사용_시각을_최신화한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            CustomizeTable table = customizeTableGenerator.generate(member);
            LocalDateTime beforeUsedAt = table.getUsedAt();

            customizeService.updateUsedAt(table.getId(), member);

            Optional<CustomizeTable> updatedTable = customizeTableRepository.findById(table.getId());
            assertAll(
                    () -> assertThat(updatedTable.get().getId()).isEqualTo(table.getId()),
                    () -> assertThat(updatedTable.get().getUsedAt()).isAfter(beforeUsedAt)
            );
        }

        @Test
        void 회원_소유가_아닌_테이블_수정_시_예외를_발생시킨다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            Member coli = memberGenerator.generate("default2@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();
            CustomizeTableCreateRequest renewTableRequest = new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("자유 테이블", "주제", "찬성",
                            "반대", true, true),
                    List.of(
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론1", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자1"),
                            new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론2", CustomizeBoxType.NORMAL,
                                    120, 60, null, "발언자2")
                    )
            );

            assertThatThrownBy(() -> customizeService.updateTable(renewTableRequest, chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class DeleteTable {

        @Test
        void 사용자_지정_토론_테이블을_삭제한다() {
            Member chan = memberGenerator.generate("default@gmail.com");
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxGenerator.generate(chanTable, CustomizeBoxType.NORMAL, 2);

            customizeService.deleteTable(chanTable.getId(), chan);

            Optional<CustomizeTable> foundTable = customizeTableRepository.findById(chanTable.getId());
            List<CustomizeTimeBox> timeBoxes = customizeTimeBoxRepository.findAllByCustomizeTable(
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
            CustomizeTable chanTable = customizeTableGenerator.generate(chan);
            long chanTableId = chanTable.getId();

            assertThatThrownBy(() -> customizeService.deleteTable(chanTableId, coli))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }
}
