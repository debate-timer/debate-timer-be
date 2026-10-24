package com.debatetimer.service.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.BellType;
import com.debatetimer.dto.customize.request.BellRequest;
import com.debatetimer.dto.customize.request.CustomizeTableCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTableInfoCreateRequest;
import com.debatetimer.dto.customize.request.CustomizeTimeBoxCreateRequest;
import com.debatetimer.service.customize.CustomizeService;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.SegmentRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.UtteranceRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class TranscriptServiceTest extends BaseServiceTest {

    @Autowired
    private TranscriptService transcriptService;

    @Autowired
    private CustomizeService customizeService;

    @MockitoBean
    private OpenAiClient openAiClient;

    private TranscriptCreateRequest requestOf(int timeBoxIndex, String... texts) {
        List<UtteranceRequest> utterances = java.util.stream.IntStream.range(0, texts.length)
                .mapToObj(i -> new UtteranceRequest(i * 1_000L, texts[i]))
                .toList();
        return new TranscriptCreateRequest(List.of(new SegmentRequest(timeBoxIndex, null, utterances)));
    }

    @Nested
    class Create {

        @Test
        void 시간표_기준으로_화자를_구분해_속기록을_저장한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);

            TranscriptCreateResponse created = transcriptService.create(table.getId(), member,
                    requestOf(0, "안녕하십니까", "찬성 입론입니다"));

            TranscriptResponse found = transcriptService.get(created.id(), member);
            assertAll(
                    () -> assertThat(found.lines()).hasSize(2),
                    () -> assertThat(found.lines().get(0).stance()).isEqualTo(Stance.PROS),
                    () -> assertThat(found.lines().get(0).speaker()).isEqualTo("콜리"),
                    () -> assertThat(found.agenda()).isEqualTo(table.getAgenda())
            );
        }

        @Test
        void 요청한_STT_모드를_함께_저장한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            List<SegmentRequest> segments = requestOf(0, "발언").segments();

            TranscriptCreateResponse created = transcriptService.create(table.getId(), member,
                    new TranscriptCreateRequest(segments, SttMode.SONIOX));

            assertThat(transcriptService.get(created.id(), member).sttMode()).isEqualTo(SttMode.SONIOX);
        }

        @Test
        void STT_모드가_없으면_RPC_모드로_저장한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);

            TranscriptCreateResponse created = transcriptService.create(table.getId(), member, requestOf(0, "발언"));

            assertThat(transcriptService.get(created.id(), member).sttMode()).isEqualTo(SttMode.RPC);
        }

        @Test
        void 인식된_발언이_없으면_예외를_던진다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);

            assertThatThrownBy(() -> transcriptService.create(table.getId(), member, requestOf(0, "  ")))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EMPTY_TRANSCRIPT.getMessage());
        }
    }

    @Nested
    class Get {

        @Test
        void 다른_회원의_기록은_조회할_수_없다() {
            Member owner = memberGenerator.generate("owner@email.com");
            Member other = memberGenerator.generate("other@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(owner);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            TranscriptCreateResponse created = transcriptService.create(table.getId(), owner, requestOf(0, "발언"));

            assertThatThrownBy(() -> transcriptService.get(created.id(), other))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TRANSCRIPT_NOT_FOUND.getMessage());
        }

        @Test
        void 분석_전_기록은_진행_중_상태다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            TranscriptCreateResponse created = transcriptService.create(table.getId(), member, requestOf(0, "발언"));

            TranscriptResponse found = transcriptService.get(created.id(), member);

            assertThat(found.status()).isIn(TranscriptStatus.PROCESSING, TranscriptStatus.DONE,
                    TranscriptStatus.FAILED);
        }
    }

    @Nested
    class FindAll {

        @Test
        void 자신의_토론_기록을_최근_생성순으로_시간표_정보와_함께_조회한다() {
            Member member = memberGenerator.generate("email@email.com");
            Member other = memberGenerator.generate("other@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            CustomizeTableEntity otherTable = customizeTableEntityGenerator.generate(other);
            customizeTimeBoxEntityGenerator.generate(otherTable, CustomizeBoxType.NORMAL, 1);
            long first = transcriptService.create(table.getId(), member, requestOf(0, "첫 기록")).id();
            long second = transcriptService.create(table.getId(), member,
                    new TranscriptCreateRequest(requestOf(0, "둘째 기록").segments(), SttMode.SONIOX)).id();
            transcriptService.create(otherTable.getId(), other, requestOf(0, "다른 회원 기록"));

            List<TranscriptSummaryResponse> transcripts = transcriptService.findAll(member).transcripts();

            assertAll(
                    () -> assertThat(transcripts).extracting(TranscriptSummaryResponse::id)
                            .containsExactly(second, first),
                    () -> assertThat(transcripts.get(0).sttMode()).isEqualTo(SttMode.SONIOX),
                    () -> assertThat(transcripts.get(0).tableName()).isEqualTo(table.getName()),
                    () -> assertThat(transcripts.get(0).agenda()).isEqualTo(table.getAgenda()),
                    () -> assertThat(transcripts.get(0).createdAt()).isNotNull()
            );
        }

        @Test
        void 기록이_없으면_빈_목록을_돌려준다() {
            Member member = memberGenerator.generate("email@email.com");

            assertThat(transcriptService.findAll(member).transcripts()).isEmpty();
        }
    }

    @Nested
    class TableSnapshot {

        private CustomizeTableCreateRequest renewedTable() {
            return new CustomizeTableCreateRequest(
                    new CustomizeTableInfoCreateRequest("바뀐 시간표", "바뀐 주제", "바뀐 찬성", "바뀐 반대", true, true),
                    List.of(new CustomizeTimeBoxCreateRequest(Stance.PROS, "입론", CustomizeBoxType.NORMAL,
                            120, List.of(new BellRequest(BellType.AFTER_START, 90, 1)), null, null, "콜리"))
            );
        }

        @Test
        void 시간표를_수정해도_속기록은_생성_시점의_시간표_이름_주제_팀_이름을_유지한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            long transcriptId = transcriptService.create(table.getId(), member, requestOf(0, "발언")).id();

            customizeService.updateTable(renewedTable(), table.getId(), member);

            TranscriptResponse found = transcriptService.get(transcriptId, member);
            TranscriptSummaryResponse summary = transcriptService.findAll(member).transcripts().get(0);
            assertAll(
                    () -> assertThat(found.tableName()).isEqualTo(table.getName()),
                    () -> assertThat(found.agenda()).isEqualTo(table.getAgenda()),
                    () -> assertThat(found.prosTeamName()).isEqualTo(table.getProsTeamName()),
                    () -> assertThat(found.consTeamName()).isEqualTo(table.getConsTeamName()),
                    () -> assertThat(summary.tableName()).isEqualTo(table.getName()),
                    () -> assertThat(summary.agenda()).isEqualTo(table.getAgenda())
            );
        }

        @Test
        void 시간표를_지워도_속기록과_목록에_생성_시점의_시간표_정보가_남는다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            long transcriptId = transcriptService.create(table.getId(), member, requestOf(0, "발언")).id();

            customizeService.deleteTable(table.getId(), member);

            TranscriptResponse found = transcriptService.get(transcriptId, member);
            TranscriptSummaryResponse summary = transcriptService.findAll(member).transcripts().get(0);
            assertAll(
                    () -> assertThat(found.agenda()).isEqualTo(table.getAgenda()),
                    () -> assertThat(summary.tableName()).isEqualTo(table.getName())
            );
        }
    }
}
