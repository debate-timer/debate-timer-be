package com.debatetimer.controller.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.dto.transcript.response.SpeechTranscribeResponse;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class SpeechTranscribeControllerTest extends BaseControllerTest {

    @MockitoBean
    private SonioxClient sonioxClient;

    @Nested
    class TranscribeWithSoniox {

        @Test
        void 발언_구간_오디오를_전사한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            when(sonioxClient.transcribe(any(), any())).thenReturn(List.of(new SpeechToken("안녕하십니까.", 300, 900)));

            SpeechTranscribeResponse response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .multiPart("audio", "segment.webm", new byte[]{1, 2, 3}, "audio/webm")
                    .formParam("language", "ko-KR")
                    .when().post("/api/stt/soniox")
                    .then().statusCode(200)
                    .extract().as(SpeechTranscribeResponse.class);

            assertThat(response.utterances()).containsExactly(new Utterance(300, "안녕하십니까."));
        }

        @Test
        void 로그인하지_않으면_전사할_수_없다() {
            given()
                    .multiPart("audio", "segment.webm", new byte[]{1, 2, 3}, "audio/webm")
                    .when().post("/api/stt/soniox")
                    .then().statusCode(401);
        }
    }
}
