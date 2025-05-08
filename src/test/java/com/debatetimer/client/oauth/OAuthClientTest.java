package com.debatetimer.client.oauth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

@RestClientTest(OAuthClient.class)
class OAuthClientTest {

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private OAuthProperties oAuthProperties;

    private OAuthClient oAuthClient;

    @BeforeEach
    void setUp() {
        this.mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        this.oAuthClient = new OAuthClient(restClientBuilder, oAuthProperties);
    }

    @Nested
    class RequestTokenExceptionHandling {

        @ParameterizedTest
        @ValueSource(strings = {
                "{\"error\":\"invalid_grant\",\"error_description\":\"Bad Request\"}",
                "{\"error\":\"invalid_grant\",\"error_description\":\"Malformed auth code.\"}",
        })
        void 잘못된_인증코드로_토큰_발급요청_시_클라이언트_에러를_반환한다(String invalidCodeResponse) {
            MemberCreateRequest memberCreateRequest = new MemberCreateRequest("invalid_code", "redirect_uri");
            setMockserver(MockRestResponseCreators.withBadRequest().body(invalidCodeResponse));

            assertThatThrownBy(() -> oAuthClient.requestToken(memberCreateRequest))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_OAUTH_REQUEST.getMessage());
        }

        @Test
        void 잘못된_리다이렉트_uri로_토큰_발급요청_시_클라이언트_에러를_반환한다() {
            MemberCreateRequest memberCreateRequest = new MemberCreateRequest("code", "invalid_uri");
            String invalidRedirectUrlResponse = "{\"error\":\"redirect_uri_mismatch\",\"error_description\":\"Bad Request\"}";
            setMockserver(MockRestResponseCreators.withBadRequest().body(invalidRedirectUrlResponse));

            assertThatThrownBy(() -> oAuthClient.requestToken(memberCreateRequest))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_OAUTH_REQUEST.getMessage());
        }

        @Test
        void 클라이언트_인증_에러는_서버에러를_반환한다() {
            MemberCreateRequest memberCreateRequest = new MemberCreateRequest("code", "redirect_url");
            String otherErrorResponse = "{\"error\":\"invalid_client\",\"error_description\":\"The OAuth client was not found.\"}";
            setMockserver(MockRestResponseCreators.withUnauthorizedRequest().body(otherErrorResponse));

            assertThatThrownBy(() -> oAuthClient.requestToken(memberCreateRequest))
                    .isInstanceOf(DTServerErrorException.class)
                    .hasMessage(ServerErrorCode.OAUTH_REQUEST_FAILED_ERROR.getMessage());
        }

        private void setMockserver(ResponseCreator responseCreator) {
            mockServer.expect(requestTo("https://oauth2.googleapis.com/token"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(responseCreator);
        }
    }
}
