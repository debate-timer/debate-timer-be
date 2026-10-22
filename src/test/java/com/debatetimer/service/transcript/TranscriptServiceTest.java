package com.debatetimer.service.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.SegmentRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.UtteranceRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
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
}
