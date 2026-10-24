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
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.service.BaseServiceTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class DebateAnalyzerTest extends BaseServiceTest {

    @Autowired
    private DebateAnalyzer debateAnalyzer;

    @Autowired
    private TranscriptDomainRepository transcriptDomainRepository;

    @MockitoBean
    private OpenAiClient openAiClient;

    private Transcript savedTranscript() {
        Member member = memberGenerator.generate("email@email.com");
        CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
        List<TranscriptLine> lines = List.of(
                new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 1_000, "정규직 과보호가 채용을 막습니다"),
                new TranscriptLine(1, 1, "입론", Stance.CONS, "비토", 65_000, "채용은 경기가 결정합니다")
        );
        return transcriptDomainRepository.create(table.getId(), member.getId(),
                TranscriptTableSnapshot.from(table.toDomain()), SttMode.RPC, lines);
    }

    @Test
    void 분석_결과를_저장하고_완료_상태로_바꾼다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn("{\"prosArguments\":[]}");

        debateAnalyzer.analyze(transcript.id());

        Transcript analyzed = transcriptDomainRepository.getById(transcript.id());
        assertAll(
                () -> assertThat(analyzed.status()).isEqualTo(TranscriptStatus.DONE),
                () -> assertThat(analyzed.reportJson()).isEqualTo("{\"prosArguments\":[]}")
        );
    }

    @Test
    void 속기록을_번호와_화자를_붙여_전달한다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn("{}");

        debateAnalyzer.analyze(transcript.id());

        verify(openAiClient).requestStructuredOutput(anyString(),
                contains("[1] (01:05) 입론 | 반대 비토: 채용은 경기가 결정합니다"), anyString(), any());
    }

    @Test
    void 분석에_실패하면_실패_상태와_사유를_남긴다() {
        Transcript transcript = savedTranscript();
        when(openAiClient.requestStructuredOutput(anyString(), anyString(), anyString(), anyMap()))
                .thenThrow(new IllegalStateException("키 없음"));

        debateAnalyzer.analyze(transcript.id());

        Transcript analyzed = transcriptDomainRepository.getById(transcript.id());
        assertAll(
                () -> assertThat(analyzed.status()).isEqualTo(TranscriptStatus.FAILED),
                () -> assertThat(analyzed.errorMessage()).contains("키 없음")
        );
    }
}
