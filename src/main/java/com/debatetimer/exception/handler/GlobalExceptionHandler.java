package com.debatetimer.exception.handler;

import com.debatetimer.client.notifier.ErrorNotifier;
import com.debatetimer.exception.ErrorResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.custom.DTOAuthClientException;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.exception.errorcode.ResponseErrorCode;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorNotifier errorNotifier;

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindingException(BindException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.FIELD_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.URL_PARAMETER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH);
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<ErrorResponse> handleClientAbortException(ClientAbortException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.ALREADY_DISCONNECTED);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<ErrorResponse> handleAsyncError(AsyncRequestNotUsableException exception) {
        if (isClientDisconnect(exception.getCause())) {
            logClientError(exception);
            return toResponse(ClientErrorCode.ALREADY_DISCONNECTED);
        }
        logServerError(exception);
        return toResponse(ServerErrorCode.INTERNAL_SERVER_ERROR);
    }

    private boolean isClientDisconnect(Throwable cause) {
        return cause instanceof IOException
                && cause.getMessage() != null
                && cause.getMessage().contains("Broken pipe");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception
    ) {
        logClientError(exception);
        return toResponse(ClientErrorCode.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception
    ) {
        logClientError(exception);
        return toResponse(ClientErrorCode.MEDIA_TYPE_NOT_SUPPORTED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.NO_RESOURCE_FOUND);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.NO_COOKIE_FOUND);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException exception) {
        logClientError(exception);
        return toResponse(ClientErrorCode.FILE_UPLOAD_ERROR);
    }

    @ExceptionHandler(DTOAuthClientException.class)
    public ResponseEntity<ErrorResponse> handleOAuthClientException(DTOAuthClientException exception) {
        logClientError(exception);
        return toResponse(exception.getHttpStatus(), exception.getMessage());
    }

    @ExceptionHandler(DTClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientException(DTClientErrorException exception) {
        logClientError(exception);
        return toResponse(exception.getHttpStatus(), exception.getMessage());
    }

    @ExceptionHandler(DTServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleServerException(DTServerErrorException exception) {
        logServerError(exception);
        return toResponse(exception.getHttpStatus(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        logServerError(exception);
        return toResponse(ServerErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void logClientError(Exception exception) {
        log.warn("exception message: {}", exception.getMessage());
    }

    private void logServerError(Exception exception) {
        log.error("exception message: {}", exception.getMessage());
        errorNotifier.sendErrorMessage(exception);
    }

    private ResponseEntity<ErrorResponse> toResponse(ResponseErrorCode errorCode) {
        return toResponse(errorCode.getStatus(), errorCode.getMessage());
    }

    private ResponseEntity<ErrorResponse> toResponse(HttpStatus httpStatus, String message) {
        ErrorResponse errorResponse = new ErrorResponse(message);
        return ResponseEntity.status(httpStatus)
                .body(errorResponse);
    }
}
