package com.debatetimer.service.admin;

import com.debatetimer.domain.prompt.CustomPrompt;
import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.domainrepository.prompt.LlmPromptDomainRepository;
import com.debatetimer.dto.admin.request.PromptUpdateRequest;
import com.debatetimer.dto.admin.response.AdminPromptResponse;
import com.debatetimer.dto.admin.response.AdminPromptsResponse;
import com.debatetimer.service.prompt.PromptProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPromptService {

    private final LlmPromptDomainRepository llmPromptDomainRepository;
    private final PromptProvider promptProvider;

    public AdminPromptsResponse findAll() {
        Map<PromptType, CustomPrompt> customs = llmPromptDomainRepository.findAllCustoms()
                .stream()
                .collect(Collectors.toMap(CustomPrompt::promptType, Function.identity()));
        List<AdminPromptResponse> prompts = Arrays.stream(PromptType.values())
                .map(type -> toResponse(type, customs.get(type)))
                .toList();
        return new AdminPromptsResponse(prompts);
    }

    // 저장이 커밋된 뒤 다시 읽어, 갱신된 수정 시각을 돌려준다
    public AdminPromptResponse update(PromptType promptType, PromptUpdateRequest request) {
        llmPromptDomainRepository.save(promptType, request.content());
        return toResponse(promptType, llmPromptDomainRepository.findCustom(promptType).orElse(null));
    }

    public void reset(PromptType promptType) {
        llmPromptDomainRepository.delete(promptType);
    }

    private AdminPromptResponse toResponse(PromptType promptType, CustomPrompt customPrompt) {
        String defaultContent = promptProvider.getDefault(promptType);
        if (customPrompt == null) {
            return AdminPromptResponse.ofDefault(promptType, defaultContent);
        }
        return AdminPromptResponse.ofCustom(customPrompt, defaultContent);
    }
}
