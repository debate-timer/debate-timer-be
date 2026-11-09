package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.prompt.PromptType;
import com.debatetimer.dto.admin.request.PromptUpdateRequest;
import com.debatetimer.dto.admin.response.AdminPromptResponse;
import com.debatetimer.dto.admin.response.AdminPromptsResponse;
import com.debatetimer.service.prompt.PromptProvider;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class AdminPromptControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";

    @Autowired
    private PromptProvider promptProvider;

    private AdminPromptsResponse getPrompts() {
        return given()
                .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                .when().get("/api/admin/prompts")
                .then().statusCode(HttpStatus.OK.value())
                .extract().as(AdminPromptsResponse.class);
    }

    private AdminPromptResponse update(PromptType promptType, String content) {
        return given()
                .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                .contentType(ContentType.JSON)
                .body(new PromptUpdateRequest(content))
                .when().put("/api/admin/prompts/{promptType}", promptType)
                .then().statusCode(HttpStatus.OK.value())
                .extract().as(AdminPromptResponse.class);
    }

    @Nested
    class GetPrompts {

        @Test
        void 수정본이_없으면_모든_프롬프트를_기본값으로_조회한다() {
            AdminPromptsResponse response = getPrompts();

            assertAll(
                    () -> assertThat(response.prompts()).extracting(AdminPromptResponse::promptType)
                            .containsExactly(PromptType.values()),
                    () -> assertThat(response.prompts()).allMatch(prompt -> !prompt.customized()
                            && prompt.content().equals(prompt.defaultContent()) && prompt.modifiedAt() == null)
            );
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .when().get("/api/admin/prompts")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class UpdatePrompt {

        @Test
        void 프롬프트를_수정하면_수정본이_서버에_반영된다() {
            AdminPromptResponse response = update(PromptType.TRANSCRIPT_REFINEMENT, "새 보정 프롬프트");

            assertAll(
                    () -> assertThat(response.customized()).isTrue(),
                    () -> assertThat(response.content()).isEqualTo("새 보정 프롬프트"),
                    () -> assertThat(response.modifiedAt()).isNotNull(),
                    () -> assertThat(promptProvider.getSystemPrompt(PromptType.TRANSCRIPT_REFINEMENT))
                            .isEqualTo("새 보정 프롬프트")
            );
        }

        @Test
        void 다시_수정하면_덮어쓴다() {
            update(PromptType.DEBATE_ANALYSIS, "첫 수정");
            update(PromptType.DEBATE_ANALYSIS, "두 번째 수정");

            assertThat(getPrompts().prompts())
                    .filteredOn(prompt -> prompt.promptType() == PromptType.DEBATE_ANALYSIS)
                    .singleElement()
                    .extracting(AdminPromptResponse::content)
                    .isEqualTo("두 번째 수정");
        }

        @Test
        void 빈_프롬프트로는_수정할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(new PromptUpdateRequest("  "))
                    .when().put("/api/admin/prompts/{promptType}", PromptType.DEBATE_ANALYSIS)
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 없는_프롬프트는_수정할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(new PromptUpdateRequest("내용"))
                    .when().put("/api/admin/prompts/UNKNOWN")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class ResetPrompt {

        @Test
        void 되돌리면_기본값을_쓴다() {
            update(PromptType.DEBATE_ANALYSIS, "수정본");

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().delete("/api/admin/prompts/{promptType}", PromptType.DEBATE_ANALYSIS)
                    .then().statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(promptProvider.getSystemPrompt(PromptType.DEBATE_ANALYSIS))
                    .isEqualTo(promptProvider.getDefault(PromptType.DEBATE_ANALYSIS));
        }
    }
}
