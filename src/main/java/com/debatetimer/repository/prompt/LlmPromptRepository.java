package com.debatetimer.repository.prompt;

import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.entity.prompt.LlmPromptEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface LlmPromptRepository extends Repository<LlmPromptEntity, Long> {

    LlmPromptEntity save(LlmPromptEntity llmPromptEntity);

    Optional<LlmPromptEntity> findByPromptType(PromptType promptType);

    List<LlmPromptEntity> findAll();

    void deleteByPromptType(PromptType promptType);
}
