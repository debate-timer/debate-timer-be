package com.debatetimer.controller.admin;

import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.dto.admin.request.PromptUpdateRequest;
import com.debatetimer.dto.admin.response.AdminPromptResponse;
import com.debatetimer.dto.admin.response.AdminPromptsResponse;
import com.debatetimer.service.admin.AdminPromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPromptController {

    private final AdminPromptService adminPromptService;

    @GetMapping("/api/admin/prompts")
    @ResponseStatus(HttpStatus.OK)
    public AdminPromptsResponse getPrompts() {
        return adminPromptService.findAll();
    }

    @PutMapping("/api/admin/prompts/{promptType}")
    @ResponseStatus(HttpStatus.OK)
    public AdminPromptResponse updatePrompt(
            @PathVariable PromptType promptType,
            @Valid @RequestBody PromptUpdateRequest request
    ) {
        return adminPromptService.update(promptType, request);
    }

    @DeleteMapping("/api/admin/prompts/{promptType}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPrompt(@PathVariable PromptType promptType) {
        adminPromptService.reset(promptType);
    }
}
