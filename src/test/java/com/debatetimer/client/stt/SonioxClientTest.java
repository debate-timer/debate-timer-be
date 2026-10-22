package com.debatetimer.client.stt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SonioxClientTest {

    private static final String BASE_URL = "https://soniox.test/v1";
    private static final Resource AUDIO = new ByteArrayResource(new byte[]{1, 2, 3}) {
        @Override
        public String getFilename() {
            return "segment.webm";
        }
    };

    private MockRestServiceServer server;
    private SonioxClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SonioxClient(builder.build(), propertiesWithKey("test-key"));
    }

    private SonioxProperties propertiesWithKey(String apiKey) {
        return new SonioxProperties(apiKey, "stt-async-v5", BASE_URL, null, Duration.ZERO, Duration.ofSeconds(5));
    }

    private void expectUploadAndCreate() {
        server.expect(requestTo(BASE_URL + "/files"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess("{\"id\":\"file-1\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/transcriptions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("stt-async-v5"))
                .andExpect(jsonPath("$.file_id").value("file-1"))
                .andExpect(jsonPath("$.language_hints[0]").value("ko"))
                .andRespond(withSuccess("{\"id\":\"tr-1\",\"status\":\"queued\"}", MediaType.APPLICATION_JSON));
    }

    private void expectStatus(String status) {
        server.expect(requestTo(BASE_URL + "/transcriptions/tr-1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":\"tr-1\",\"status\":\"%s\",\"error_message\":\"bad audio\"}"
                        .formatted(status), MediaType.APPLICATION_JSON));
    }

    private void expectCleanUp() {
        server.expect(requestTo(BASE_URL + "/transcriptions/tr-1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL + "/files/file-1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
    }

    @Nested
    class Transcribe {

        @Test
        void 전사가_끝날_때까지_기다린_뒤_토큰을_돌려주고_리소스를_지운다() {
            expectUploadAndCreate();
            expectStatus("processing");
            expectStatus("completed");
            server.expect(requestTo(BASE_URL + "/transcriptions/tr-1/transcript"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"id":"tr-1","text":"안녕","tokens":[
                              {"text":"안","start_ms":100,"end_ms":200,"confidence":0.9},
                              {"text":"녕","start_ms":200,"end_ms":300,"confidence":0.9},
                              {"text":"hi","start_ms":100,"end_ms":300,"translation_status":"translation"}
                            ]}
                            """, MediaType.APPLICATION_JSON));
            expectCleanUp();

            List<SpeechToken> tokens = client.transcribe(AUDIO, List.of("ko"));

            assertThat(tokens).containsExactly(
                    new SpeechToken("안", 100, 200),
                    new SpeechToken("녕", 200, 300)
            );
            server.verify();
        }

        @Test
        void 전사가_실패하면_예외를_던지고_리소스를_지운다() {
            expectUploadAndCreate();
            expectStatus("error");
            expectCleanUp();

            assertThatThrownBy(() -> client.transcribe(AUDIO, List.of("ko")))
                    .isInstanceOf(DTServerErrorException.class)
                    .hasMessage(ServerErrorCode.STT_REQUEST_FAILED.getMessage());
            server.verify();
        }

        @Test
        void 업로드가_실패하면_예외를_던진다() {
            server.expect(requestTo(BASE_URL + "/files"))
                    .andRespond(withServerError());

            assertThatThrownBy(() -> client.transcribe(AUDIO, List.of("ko")))
                    .isInstanceOf(DTServerErrorException.class)
                    .hasMessage(ServerErrorCode.STT_REQUEST_FAILED.getMessage());
            server.verify();
        }

        @Test
        void API_키가_없으면_요청하지_않고_예외를_던진다() {
            SonioxClient noKeyClient = new SonioxClient(RestClient.builder().build(), propertiesWithKey(" "));

            assertThatThrownBy(() -> noKeyClient.transcribe(AUDIO, List.of("ko")))
                    .isInstanceOf(DTServerErrorException.class)
                    .hasMessage(ServerErrorCode.STT_NOT_CONFIGURED.getMessage());
        }
    }
}
