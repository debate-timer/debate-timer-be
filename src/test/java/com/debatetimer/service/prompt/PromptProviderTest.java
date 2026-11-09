package com.debatetimer.service.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.domainrepository.prompt.LlmPromptDomainRepository;
import com.debatetimer.service.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PromptProviderTest extends BaseServiceTest {

    @Autowired
    private PromptProvider promptProvider;

    @Autowired
    private LlmPromptDomainRepository llmPromptDomainRepository;

    @Test
    void 수정본이_없으면_md_파일_기본값을_쓴다() {
        String prompt = promptProvider.getSystemPrompt(PromptType.TRANSCRIPT_REFINEMENT);

        assertThat(prompt).startsWith("# 속기록 보정")
                .isEqualTo(promptProvider.getDefault(PromptType.TRANSCRIPT_REFINEMENT));
    }

    @Test
    void 수정본이_있으면_수정본을_쓴다() {
        llmPromptDomainRepository.save(PromptType.DEBATE_ANALYSIS, "수정한 분석 프롬프트");

        assertThat(promptProvider.getSystemPrompt(PromptType.DEBATE_ANALYSIS)).isEqualTo("수정한 분석 프롬프트");
    }

    @Test
    void 수정본을_지우면_기본값으로_돌아간다() {
        llmPromptDomainRepository.save(PromptType.DEBATE_ANALYSIS, "수정한 분석 프롬프트");
        llmPromptDomainRepository.delete(PromptType.DEBATE_ANALYSIS);

        assertThat(promptProvider.getSystemPrompt(PromptType.DEBATE_ANALYSIS))
                .isEqualTo(promptProvider.getDefault(PromptType.DEBATE_ANALYSIS));
    }
}
