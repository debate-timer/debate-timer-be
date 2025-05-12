package com.debatetimer.exception.handler;

import com.debatetimer.exception.OAuthErrorResponse;
import com.debatetimer.exception.custom.DTOAuthClientException;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.OAuthErrorCode;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

@Component
public class OAuthClientErrorHandler implements ErrorHandler {

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OAuthErrorResponse errorResponse = objectMapper.readValue(response.getBody(), OAuthErrorResponse.class);

        Optional<OAuthErrorCode> oauthClientErrorCode = OAuthErrorCode.mapTo(errorResponse.error());

        if (oauthClientErrorCode.isPresent()) {
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            throw new DTOAuthClientException(oauthClientErrorCode.get(), responseBody);
        }
        throw new DTServerErrorException(ServerErrorCode.OAUTH_REQUEST_FAILED_ERROR);
    }
}
