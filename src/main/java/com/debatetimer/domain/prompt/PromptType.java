package com.debatetimer.domain.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 어드민에서 고칠 수 있는 LLM 시스템 프롬프트. 기본값은 defaultPath 의 md 파일이고, DB 에 수정본이 있으면 그것을 쓴다.
 * DB 에는 이름(문자열)으로 저장하므로 값을 지우거나 이름을 바꿀 때는 저장된 행도 함께 옮긴다.
 */
@Getter
@RequiredArgsConstructor
public enum PromptType {

    TRANSCRIPT_REFINEMENT("속기록 보정", "prompt/transcript-refinement-system.md"),
    DEBATE_ANALYSIS("토론 분석 보고서", "prompt/debate-analysis-system.md"),
    ;

    private final String description;
    private final String defaultPath;
}
