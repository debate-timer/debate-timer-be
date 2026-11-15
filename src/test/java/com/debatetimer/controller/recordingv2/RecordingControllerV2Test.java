package com.debatetimer.controller.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.dto.recordingv2.request.RecordingJoinRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingSessionCreateRequestV2;
import com.debatetimer.dto.recordingv2.response.RecordingDeviceResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionCreateResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionResponseV2;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.scheduler.recordingv2.RecordingV2Scheduler;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = {
        "recording-v2.enabled=true",
        "recording-v2.spool-dir=build/test-recording-v2",
        "recording-v2.max-devices=3"
})
class RecordingControllerV2Test extends BaseControllerTest {

    // 주기 작업이 테스트 중에 턴을 닫아 전사로 넘기지 않게 막는다
    @MockitoBean
    private RecordingV2Scheduler recordingV2Scheduler;

    private Member member;
    private CustomizeTableEntity table;

    @BeforeEach
    void setUpTable() {
        member = memberGenerator.generate("host@gmail.com");
        table = customizeTableEntityGenerator.generate(member);
        customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
    }

    @Nested
    class CreateSession {

        @Test
        void 플래그가_있는_회원은_녹음_세션을_연다() {
            memberFeatureFlagEntityGenerator.generate(member, FeatureFlag.MULTI_DEVICE_RECORDING);

            RecordingSessionCreateResponseV2 response = createSession();

            assertThat(response.sessionKey()).isNotBlank();
        }

        @Test
        void 플래그가_없으면_열_수_없다() {
            given()
                    .headers(headerGenerator.generateAccessTokenHeader(member))
                    .contentType(ContentType.JSON)
                    .body(new RecordingSessionCreateRequestV2(table.getId(), null))
                    .when().post("/api/v2/recordings")
                    .then().statusCode(403);
        }
    }

    @Nested
    class UploadTurn {

        @Test
        void 청중은_참가_코드로_참가해_로그인_없이_턴을_올린다() {
            memberFeatureFlagEntityGenerator.generate(member, FeatureFlag.MULTI_DEVICE_RECORDING);
            RecordingSessionCreateResponseV2 session = createSession();

            RecordingDeviceResponseV2 audience = given()
                    .contentType(ContentType.JSON)
                    .body(new RecordingJoinRequestV2(session.joinCode()))
                    .when().post("/api/v2/recordings/{sessionKey}/devices", session.sessionKey())
                    .then().statusCode(201)
                    .extract().as(RecordingDeviceResponseV2.class);

            upload(session.sessionKey(), audience.deviceToken())
                    .when().post("/api/v2/recordings/{sessionKey}/turns/{turnKey}", session.sessionKey(), "0-1")
                    .then().statusCode(202);
        }

        @Test
        void 기기_토큰이_없으면_올릴_수_없다() {
            memberFeatureFlagEntityGenerator.generate(member, FeatureFlag.MULTI_DEVICE_RECORDING);
            RecordingSessionCreateResponseV2 session = createSession();

            upload(session.sessionKey(), null)
                    .when().post("/api/v2/recordings/{sessionKey}/turns/{turnKey}", session.sessionKey(), "0-1")
                    .then().statusCode(403);
        }

        @Test
        void 필수_폼_값이_없으면_올릴_수_없다() {
            memberFeatureFlagEntityGenerator.generate(member, FeatureFlag.MULTI_DEVICE_RECORDING);
            RecordingSessionCreateResponseV2 session = createSession();

            given()
                    .header(RecordingControllerV2.DEVICE_TOKEN_HEADER, session.deviceToken())
                    .multiPart("audio", "turn.webm", new byte[40_000], "audio/webm")
                    .when().post("/api/v2/recordings/{sessionKey}/turns/{turnKey}", session.sessionKey(), "0-1")
                    .then().statusCode(400);
        }
    }

    @Nested
    class Finish {

        @Test
        void 사회자가_끝내면_종료_중_상태가_된다() {
            memberFeatureFlagEntityGenerator.generate(member, FeatureFlag.MULTI_DEVICE_RECORDING);
            RecordingSessionCreateResponseV2 session = createSession();

            given()
                    .headers(headerGenerator.generateAccessTokenHeader(member))
                    .when().post("/api/v2/recordings/{sessionKey}/finish", session.sessionKey())
                    .then().statusCode(202);

            RecordingSessionResponseV2 response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(member))
                    .when().get("/api/v2/recordings/{sessionKey}", session.sessionKey())
                    .then().statusCode(200)
                    .extract().as(RecordingSessionResponseV2.class);
            assertThat(response.status()).isEqualTo(RecordingSessionStatusV2.FINISHING);
        }
    }

    private RecordingSessionCreateResponseV2 createSession() {
        return given()
                .headers(headerGenerator.generateAccessTokenHeader(member))
                .contentType(ContentType.JSON)
                .body(new RecordingSessionCreateRequestV2(table.getId(), "ko-KR"))
                .when().post("/api/v2/recordings")
                .then().statusCode(201)
                .extract().as(RecordingSessionCreateResponseV2.class);
    }

    private RequestSpecification upload(String sessionKey, String deviceToken) {
        RequestSpecification request = given()
                .multiPart("audio", "turn.webm", new byte[40_000], "audio/webm")
                .formParam("timeBoxIndex", 0)
                .formParam("durationMs", 10_000)
                .formParam("startedAtEpochMs", 1_000)
                .formParam("rmsDb", -20.0)
                .formParam("peakDb", -3.0)
                .formParam("clippingRatio", 0.0)
                .formParam("silenceRatio", 0.2)
                .formParam("snrDb", 30.0);
        if (deviceToken != null) {
            request.header(RecordingControllerV2.DEVICE_TOKEN_HEADER, deviceToken);
        }
        return request;
    }
}
