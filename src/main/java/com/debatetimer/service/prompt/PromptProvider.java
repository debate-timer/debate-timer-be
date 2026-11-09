package com.debatetimer.service.prompt;

import com.debatetimer.domain.prompt.CustomPrompt;
import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.domainrepository.prompt.LlmPromptDomainRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * LLM 시스템 프롬프트를 준다. 어드민 수정본이 DB 에 있으면 그것을, 없으면 md 파일 기본값을 쓴다.
 * 호출할 때마다 DB 를 읽어 수정본이 재배포 없이 모든 서버에 바로 반영되게 한다.
 */
@Service
public class PromptProvider {

    private final LlmPromptDomainRepository llmPromptDomainRepository;
    private final Map<PromptType, String> defaults;

    public PromptProvider(LlmPromptDomainRepository llmPromptDomainRepository) {
        this.llmPromptDomainRepository = llmPromptDomainRepository;
        this.defaults = Arrays.stream(PromptType.values())
                .collect(Collectors.toMap(Function.identity(), type -> readResource(type.getDefaultPath()),
                        (first, second) -> first, () -> new EnumMap<>(PromptType.class)));
    }

    public String getSystemPrompt(PromptType promptType) {
        return llmPromptDomainRepository.findCustom(promptType)
                .map(CustomPrompt::content)
                .orElseGet(() -> getDefault(promptType));
    }

    public String getDefault(PromptType promptType) {
        return defaults.get(promptType);
    }

    private String readResource(String path) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
