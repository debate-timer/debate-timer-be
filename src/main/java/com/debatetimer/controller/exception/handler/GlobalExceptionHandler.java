package com.debatetimer.controller.exception.handler;

import com.debatetimer.controller.exception.ErrorResponse;
import com.debatetimer.controller.exception.custom.DTClientErrorException;
import com.debatetimer.controller.exception.custom.DTServerErrorException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        String exceptionMessage = exception.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" | "));
        log.warn("message: {}", exceptionMessage);
        return toResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindingException(BindException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<ErrorResponse> handleClientAbortException(ClientAbortException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(HttpStatus.BAD_REQUEST, "");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(HttpStatus.METHOD_NOT_ALLOWED, "");
    }

    @ExceptionHandler()
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        return toResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DTClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientException(DTServerErrorException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(exception.getHttpStatus(), exception.getMessage());
    }

    @ExceptionHandler(DTServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleServerException(DTServerErrorException exception) {
        log.warn("message: {}", exception.getMessage());
        return toResponse(exception.getHttpStatus(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("exception: {}", exception);
        return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류");
    }

    private ResponseEntity<ErrorResponse> toResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(message);
        return ResponseEntity.status(status)
                .body(errorResponse);
    }
}
