package com.debatetimer.domainrepository.prompt;

import com.debatetimer.domain.prompt.CustomPrompt;
import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.entity.prompt.LlmPromptEntity;
import com.debatetimer.repository.prompt.LlmPromptRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class LlmPromptDomainRepository {

    private final LlmPromptRepository llmPromptRepository;

    @Transactional(readOnly = true)
    public Optional<CustomPrompt> findCustom(PromptType promptType) {
        return llmPromptRepository.findByPromptType(promptType)
                .map(LlmPromptEntity::toDomain);
    }

    @Transactional(readOnly = true)
    public List<CustomPrompt> findAllCustoms() {
        return llmPromptRepository.findAll()
                .stream()
                .map(LlmPromptEntity::toDomain)
                .toList();
    }

    // 수정본이 있으면 덮어쓰고, 없으면 새로 만든다
    @Transactional
    public void save(PromptType promptType, String content) {
        llmPromptRepository.findByPromptType(promptType)
                .ifPresentOrElse(entity -> entity.updateContent(content),
                        () -> llmPromptRepository.save(new LlmPromptEntity(promptType, content)));
    }

    @Transactional
    public void delete(PromptType promptType) {
        llmPromptRepository.deleteByPromptType(promptType);
    }
}
