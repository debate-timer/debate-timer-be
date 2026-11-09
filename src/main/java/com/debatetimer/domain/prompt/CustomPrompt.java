package com.debatetimer.domain.prompt;

import java.time.LocalDateTime;

/**
 * 어드민에서 수정해 DB 에 저장한 프롬프트.
 */
public record CustomPrompt(PromptType promptType, String content, LocalDateTime modifiedAt) {
}
