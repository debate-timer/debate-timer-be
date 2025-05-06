package com.debatetimer.exception.handler;

import com.debatetimer.exception.OAuthErrorResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

@Component
public class OAuthClientErrorHandler implements ErrorHandler {

    private final String INVALID_REDIRECT_URI_PREFIX = "Invalid redirect_uri";
    private final String INVALID_AUTHORIZATION_CODE_MESSAGE = "Invalid Authorization Code";

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        if (isClientError(response)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_OAUTH_REQUEST);
        }

        throw new DTServerErrorException(ServerErrorCode.OAUTH_REQUEST_FAILED_ERROR);
    }

    private boolean isClientError(ClientHttpResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OAuthErrorResponse errorResponse = objectMapper.readValue(response.getBody(), OAuthErrorResponse.class);

        String error = errorResponse.error();
        return error.startsWith(INVALID_REDIRECT_URI_PREFIX)
                || error.equals(INVALID_AUTHORIZATION_CODE_MESSAGE);
    }
}
