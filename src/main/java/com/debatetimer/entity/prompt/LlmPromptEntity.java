package com.debatetimer.entity.prompt;

import com.debatetimer.domain.prompt.CustomPrompt;
import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "llm_prompt", uniqueConstraints = @UniqueConstraint(columnNames = "prompt_type"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmPromptEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", length = 50)
    private PromptType promptType;

    @NotNull
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    public LlmPromptEntity(PromptType promptType, String content) {
        this.promptType = promptType;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public CustomPrompt toDomain() {
        return new CustomPrompt(promptType, content, getModifiedAt());
    }
}
