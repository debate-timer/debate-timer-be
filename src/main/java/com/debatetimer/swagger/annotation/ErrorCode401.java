package com.debatetimer.swagger.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
)
public @interface ErrorCode401 {

    @AliasFor(annotation = ApiResponse.class, attribute = "description")
    String description() default "인증되지 않은 사용자";
}
