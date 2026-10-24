package com.debatetimer.service.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptTableSnapshot;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.service.BaseServiceTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class TranscriptRefinerTest extends BaseServiceTest {

    private static final List<TranscriptLine> RAW_LINES = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 1_000, "안녕하십니까 찬송추 콜리입니다"),
            new TranscriptLine(1, 0, "입론", Stance.PROS, "콜리", 3_000, "어..."),
            new TranscriptLine(2, 1, "입론", Stance.CONS, "비토", 65_000, "채찍 pt는 위험합니다")
    );

    @Autowired
    private TranscriptRefiner transcriptRefiner;

    @Autowired
    private TranscriptDomainRepository transcriptDomainRepository;

    @MockitoBean
    private OpenAiClient openAiClient;

    private Transcript savedTranscript() {
        Member member = memberGenerator.generate("email@email.com");
        CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
        return transcriptDomainRepository.create(table.getId(), member.getId(),
                TranscriptTableSnapshot.from(table.toDomain()), SttMode.RPC, RAW_LINES);
    }

    @Test
    void 다듬은_문장을_속기록에_반영하고_원문은_보관한다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn("""
                        {"lines":[
                          {"index":0,"text":"안녕하십니까, 찬성 측 콜리입니다."},
                          {"index":1,"text":""},
                          {"index":2,"text":"ChatGPT는 위험합니다."}
                        ]}
                        """);

        transcriptRefiner.refine(transcript.id());

        Transcript refined = transcriptDomainRepository.getById(transcript.id());
        assertAll(
                () -> assertThat(refined.refining()).isFalse(),
                () -> assertThat(refined.lines()).extracting(TranscriptLine::text)
                        .containsExactly("안녕하십니까, 찬성 측 콜리입니다.", "ChatGPT는 위험합니다."),
                () -> assertThat(refined.lines().get(1).speaker()).isEqualTo("비토"),
                () -> assertThat(refined.rawLines()).isEqualTo(RAW_LINES)
        );
    }

    @Test
    void 주제와_번호_화자를_붙여_원문을_전달한다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn("{\"lines\":[]}");

        transcriptRefiner.refine(transcript.id());

        verify(openAiClient).requestStructuredOutput(anyString(),
                contains("[2] 입론 | 반대 비토: 채찍 pt는 위험합니다"), anyString(), any());
    }

    @Test
    void 다듬기에_실패하면_원문을_속기록으로_쓴다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenThrow(new IllegalStateException("키 없음"));

        transcriptRefiner.refine(transcript.id());

        Transcript skipped = transcriptDomainRepository.getById(transcript.id());
        assertAll(
                () -> assertThat(skipped.refining()).isFalse(),
                () -> assertThat(skipped.lines()).isEqualTo(RAW_LINES)
        );
    }

    @Test
    void 응답_형식이_잘못되면_원문을_속기록으로_쓴다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn("not json");

        transcriptRefiner.refine(transcript.id());

        Transcript skipped = transcriptDomainRepository.getById(transcript.id());
        assertAll(
                () -> assertThat(skipped.refining()).isFalse(),
                () -> assertThat(skipped.lines()).isEqualTo(RAW_LINES)
        );
    }
}
