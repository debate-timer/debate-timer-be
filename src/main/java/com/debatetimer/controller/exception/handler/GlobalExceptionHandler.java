package com.debatetimer.controller.exception.handler;

import com.debatetimer.controller.exception.ErrorResponse;
import com.debatetimer.controller.exception.custom.DTException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String exceptionMessage = exception.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" | "));
        log.warn("message: {}", exceptionMessage);
        return toResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        return toResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DTException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(DTException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(exception.getHttpStatus(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        log.error("exception: {}", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러");
    }

    private ResponseEntity<ErrorResponse> toResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(message);
        return ResponseEntity.status(status)
                .body(errorResponse);
    }
}
