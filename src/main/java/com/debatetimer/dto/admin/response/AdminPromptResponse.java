package com.debatetimer.dto.admin.response;

import com.debatetimer.domain.prompt.CustomPrompt;
import com.debatetimer.domain.prompt.PromptType;
import java.time.LocalDateTime;

/**
 * customized 가 false 면 content 는 기본값과 같고 modifiedAt 은 null 이다.
 */
public record AdminPromptResponse(
        PromptType promptType,
        String description,
        String content,
        String defaultContent,
        boolean customized,
        LocalDateTime modifiedAt
) {

    public static AdminPromptResponse ofDefault(PromptType promptType, String defaultContent) {
        return new AdminPromptResponse(promptType, promptType.getDescription(), defaultContent, defaultContent,
                false, null);
    }

    public static AdminPromptResponse ofCustom(CustomPrompt customPrompt, String defaultContent) {
        PromptType promptType = customPrompt.promptType();
        return new AdminPromptResponse(promptType, promptType.getDescription(), customPrompt.content(),
                defaultContent, true, customPrompt.modifiedAt());
    }
}
