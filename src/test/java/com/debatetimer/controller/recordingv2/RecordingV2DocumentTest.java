package com.debatetimer.controller.recordingv2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.dto.recordingv2.request.RecordingJoinRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingSessionCreateRequestV2;
import com.debatetimer.dto.recordingv2.response.RecordingDeviceResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionCreateResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionResponseV2.TurnResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.recordingv2.RecordingServiceV2;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class RecordingV2DocumentTest extends BaseDocumentTest {

    private static final String SESSION_KEY = "3f2b8c1e-5d4a-4e7b-9c0d-1a2b3c4d5e6f";
    private static final String JOIN_CODE = "Xq2pL0aZ8rTk3mWd";
    private static final String DEVICE_TOKEN = "Zt0mY2v9kq3LxR8sWb1nC4pQ7eF6gH5jAa0dS2uV9i";

    @MockitoBean
    private RecordingServiceV2 recordingServiceV2;

    @Nested
    class CreateSession {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.RECORDING_V2_API)
                .summary(featureFlagSummary(FeatureFlag.MULTI_DEVICE_RECORDING, "다기기 녹음 세션 생성"))
                .description(featureFlagDescription(FeatureFlag.MULTI_DEVICE_RECORDING) + """
                        사회자가 토론 녹음을 시작할 때 녹음 세션을 엽니다. 사회자 기기는 응답의 deviceToken으로 바로 턴 녹음을 올립니다.
                        joinCode는 청중 기기에 공유 링크·QR로 넘겨 참가에 씁니다. joinCode와 deviceToken은 다시 조회할 수 없습니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .requestBodyField(
                        fieldWithPath("tableId").type(NUMBER).description("토론 시간표 ID"),
                        fieldWithPath("language").type(STRING).optional().description("화면 언어 (ko-KR, en-US 등)")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("sessionKey").type(STRING).description("녹음 세션 식별자"),
                        fieldWithPath("joinCode").type(STRING).description("청중 기기 참가 코드"),
                        fieldWithPath("deviceId").type(STRING).description("사회자 기기 ID"),
                        fieldWithPath("deviceToken").type(STRING).description("사회자 기기 업로드 토큰")
                );

        @Test
        void 다기기_녹음_세션_생성_성공() {
            doReturn(new RecordingSessionCreateResponseV2(SESSION_KEY, JOIN_CODE, "hOsT01", DEVICE_TOKEN))
                    .when(recordingServiceV2).create(any(), any());

            var document = document("recording-v2/create", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .contentType(ContentType.JSON)
                    .body(new RecordingSessionCreateRequestV2(1L, "ko-KR"))
                    .when().post("/api/v2/recordings")
                    .then().statusCode(201);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"TABLE_NOT_FOUND", "RECORDING_V2_DISABLED"})
        @ParameterizedTest
        void 다기기_녹음_세션_생성_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(recordingServiceV2).create(any(), any());

            var document = document("recording-v2/create", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .contentType(ContentType.JSON)
                    .body(new RecordingSessionCreateRequestV2(1L, "ko-KR"))
                    .when().post("/api/v2/recordings")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class Join {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.RECORDING_V2_API)
                .summary("다기기 녹음 청중 기기 참가")
                .description("""
                        청중 기기가 로그인 없이 참가 코드로 녹음 세션에 참가합니다. 응답의 deviceToken으로 턴 녹음을 올립니다.
                        사회자 기기를 포함해 세션당 참가할 수 있는 기기 수에 제한이 있습니다.
                        """)
                .pathParameter(
                        parameterWithName("sessionKey").description("녹음 세션 식별자")
                )
                .requestBodyField(
                        fieldWithPath("joinCode").type(STRING).description("참가 코드")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("deviceId").type(STRING).description("기기 ID"),
                        fieldWithPath("deviceToken").type(STRING).description("기기 업로드 토큰")
                );

        @Test
        void 다기기_녹음_청중_기기_참가_성공() {
            doReturn(new RecordingDeviceResponseV2("aUdI01", DEVICE_TOKEN))
                    .when(recordingServiceV2).join(eq(SESSION_KEY), any());

            var document = document("recording-v2/join", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(new RecordingJoinRequestV2(JOIN_CODE))
                    .when().post("/api/v2/recordings/{sessionKey}/devices", SESSION_KEY)
                    .then().statusCode(201);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"RECORDING_SESSION_NOT_FOUND",
                "INVALID_RECORDING_JOIN_CODE", "RECORDING_SESSION_CLOSED", "RECORDING_DEVICE_LIMIT_EXCEEDED"})
        @ParameterizedTest
        void 다기기_녹음_청중_기기_참가_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(recordingServiceV2).join(anyString(), any());

            var document = document("recording-v2/join", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(new RecordingJoinRequestV2(JOIN_CODE))
                    .when().post("/api/v2/recordings/{sessionKey}/devices", SESSION_KEY)
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class UploadTurn {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.RECORDING_V2_API)
                .summary("다기기 녹음 턴 오디오 업로드")
                .description("""
                        참가한 기기가 타이머가 끝난 턴(발언 구간)의 녹음을 올립니다. 받아 두기만 하고 바로 202로 응답합니다.
                        같은 턴은 모든 기기가 같은 turnKey로 올립니다. turnKey는 `{타임박스 순서}-{타이머를 시작한 공유 이벤트 version}` 처럼
                        모든 기기가 공유 이벤트로 똑같이 만들 수 있는 값으로, 영문·숫자·'_'·'-' 64자 이하입니다.

                        첫 후보가 도착하고 참가 기기가 모두 올리거나 20초가 지나면 턴을 마감하고, 음성 품질이 가장 좋은 후보 하나만
                        S3 `stt-audio-v2/` 아래에 보관하고 전사합니다. 마감된 턴에 올리면 `409 RECORDING_TURN_CLOSED` 를 응답합니다.
                        같은 기기가 마감 전에 다시 올리면 새 오디오로 바꿉니다.

                        ### multipart/form-data 파트
                        | 파트 | 필수 | 설명 |
                        | :---: | :---: | --- |
                        | audio | O | 턴 오디오 파일 (webm, mp4 등, 최대 30MB) |
                        | timeBoxIndex | O | 타임박스 순서 (0부터) |
                        | team | X | 자유토론처럼 한 타임박스 안에서 발언 팀이 바뀌는 경우의 발언 팀 (PROS, CONS) |
                        | durationMs | O | 녹음 길이(ms) |
                        | startedAtEpochMs | O | 녹음을 시작한 시각. 공유 이벤트의 serverTime으로 추정한 서버 시계 기준 epoch ms |
                        | rmsDb | X | 평균 음량(dBFS, 0 이하) |
                        | peakDb | X | 최대 순간 음량(dBFS, 0 이하) |
                        | clippingRatio | X | 최대 진폭에 닿은 샘플 비율(0~1) |
                        | silenceRatio | X | 무음 구간 비율(0~1) |
                        | snrDb | X | 발화 음량과 잡음 바닥의 차이(dB) |

                        품질 지표는 Web Audio로 녹음하며 계산해 보냅니다. 하나라도 빠지거나 범위를 벗어나면 지표가 있는 후보보다 뒤로 밀립니다.
                        """)
                .pathParameter(
                        parameterWithName("sessionKey").description("녹음 세션 식별자"),
                        parameterWithName("turnKey").description("턴 식별자")
                )
                .requestHeader(
                        headerWithName(RecordingControllerV2.DEVICE_TOKEN_HEADER).description("기기 업로드 토큰")
                )
                .requestPart(
                        partWithName("audio").description("턴 오디오 파일"),
                        partWithName("timeBoxIndex").description("타임박스 순서"),
                        partWithName("team").optional().description("발언 팀"),
                        partWithName("durationMs").description("녹음 길이(ms)"),
                        partWithName("startedAtEpochMs").description("녹음 시작 시각(epoch ms)"),
                        partWithName("rmsDb").optional().description("평균 음량(dBFS)"),
                        partWithName("peakDb").optional().description("최대 순간 음량(dBFS)"),
                        partWithName("clippingRatio").optional().description("클리핑 샘플 비율"),
                        partWithName("silenceRatio").optional().description("무음 구간 비율"),
                        partWithName("snrDb").optional().description("SNR(dB)")
                );

        @Test
        void 다기기_녹음_턴_오디오_업로드_성공() {
            var document = document("recording-v2/upload-turn", 202)
                    .request(requestDocument)
                    .build();

            uploadRequest(document)
                    .then().statusCode(202);
        }

        @EnumSource(value = ClientErrorCode.class, names = {"INVALID_RECORDING_DEVICE", "INVALID_RECORDING_TURN",
                "RECORDING_TURN_CLOSED", "RECORDING_SESSION_CLOSED", "EMPTY_AUDIO", "RECORDING_BUSY"})
        @ParameterizedTest
        void 다기기_녹음_턴_오디오_업로드_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(recordingServiceV2)
                    .upload(anyString(), any(), anyString(), any(), any());

            var document = document("recording-v2/upload-turn", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            uploadRequest(document)
                    .then().statusCode(errorCode.getStatus().value());
        }

        private Response uploadRequest(RestDocumentationFilter document) {
            return given(document)
                    .header(RecordingControllerV2.DEVICE_TOKEN_HEADER, DEVICE_TOKEN)
                    .multiPart("audio", "turn.webm", new byte[]{1, 2, 3}, "audio/webm")
                    .multiPart("timeBoxIndex", "0")
                    .multiPart("team", "PROS")
                    .multiPart("durationMs", "180000")
                    .multiPart("startedAtEpochMs", "1762905600000")
                    .multiPart("rmsDb", "-21.5")
                    .multiPart("peakDb", "-2.1")
                    .multiPart("clippingRatio", "0.0")
                    .multiPart("silenceRatio", "0.18")
                    .multiPart("snrDb", "28.4")
                    .when().post("/api/v2/recordings/{sessionKey}/turns/{turnKey}", SESSION_KEY, "0-12");
        }
    }

    @Nested
    class GetSession {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.RECORDING_V2_API)
                .summary(featureFlagSummary(FeatureFlag.MULTI_DEVICE_RECORDING, "다기기 녹음 세션 상태 조회"))
                .description(featureFlagDescription(FeatureFlag.MULTI_DEVICE_RECORDING) + """
                        사회자가 녹음 세션과 턴별 처리 상태를 조회합니다. 끝낸 뒤 이 API를 폴링하다가 status가 COMPLETED가 되면
                        transcriptId로 기존 속기록 조회 API를 호출합니다.

                        ### 세션 상태
                        | 상태 | 설명 |
                        | :---: | --- |
                        | RECORDING | 기기들이 턴 녹음을 올리는 중 |
                        | FINISHING | 끝낸 뒤 남은 턴 전사를 기다리는 중 |
                        | COMPLETED | 속기록을 만들었음 |
                        | FAILED | 전사된 발언이 없거나 속기록을 만들지 못했음 |
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("sessionKey").description("녹음 세션 식별자")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("sessionKey").type(STRING).description("녹음 세션 식별자"),
                        fieldWithPath("status").type(STRING).description("세션 상태"),
                        fieldWithPath("transcriptId").type(NUMBER).optional().description("만든 속기록 ID"),
                        fieldWithPath("errorMessage").type(STRING).optional().description("실패 사유"),
                        fieldWithPath("turns").type(ARRAY).description("마감된 턴 목록"),
                        fieldWithPath("turns[].turnKey").type(STRING).description("턴 식별자"),
                        fieldWithPath("turns[].timeBoxIndex").type(NUMBER).description("타임박스 순서"),
                        fieldWithPath("turns[].team").type(STRING).optional().description("발언 팀"),
                        fieldWithPath("turns[].status").type(STRING).description("턴 상태 (PROCESSING, DONE, FAILED)"),
                        fieldWithPath("turns[].candidateCount").type(NUMBER).description("모인 후보 수"),
                        fieldWithPath("turns[].selectedDeviceId").type(STRING).optional()
                                .description("전사에 쓴 기기 ID"),
                        fieldWithPath("turns[].errorMessage").type(STRING).optional().description("실패 사유")
                );

        @Test
        void 다기기_녹음_세션_상태_조회_성공() {
            RecordingSessionResponseV2 response = new RecordingSessionResponseV2(SESSION_KEY,
                    RecordingSessionStatusV2.COMPLETED, 42L, null, List.of(
                    new TurnResponse("0-12", 0, Stance.PROS, RecordingTurnStatusV2.DONE, 3, "aUdI01", null),
                    new TurnResponse("1-15", 1, Stance.CONS, RecordingTurnStatusV2.FAILED, 2, null,
                            "모든 녹음 후보의 전사에 실패했습니다.")
            ));
            doReturn(response).when(recordingServiceV2).get(any(), eq(SESSION_KEY));

            var document = document("recording-v2/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().get("/api/v2/recordings/{sessionKey}", SESSION_KEY)
                    .then().statusCode(200);
        }
    }

    @Nested
    class Finish {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.RECORDING_V2_API)
                .summary(featureFlagSummary(FeatureFlag.MULTI_DEVICE_RECORDING, "다기기 녹음 종료"))
                .description(featureFlagDescription(FeatureFlag.MULTI_DEVICE_RECORDING) + """
                        사회자가 토론을 끝냅니다. 청중 기기의 마지막 턴이 늦게 도착할 수 있어 20초 동안은 업로드를 더 받고,
                        남은 턴 처리가 모두 끝나면 서버가 속기록을 만듭니다. 이미 끝낸 세션이면 그대로 202를 응답합니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("sessionKey").description("녹음 세션 식별자")
                );

        @Test
        void 다기기_녹음_종료_성공() {
            var document = document("recording-v2/finish", 202)
                    .request(requestDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().post("/api/v2/recordings/{sessionKey}/finish", SESSION_KEY)
                    .then().statusCode(202);
        }
    }

    @Nested
    class RejoinHost {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.RECORDING_V2_API)
                .summary(featureFlagSummary(FeatureFlag.MULTI_DEVICE_RECORDING, "다기기 녹음 사회자 기기 재참가"))
                .description(featureFlagDescription(FeatureFlag.MULTI_DEVICE_RECORDING) + """
                        사회자 기기가 새로고침이나 서버 재시작으로 업로드 토큰을 잃었을 때 사회자 기기로 다시 참가합니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("sessionKey").description("녹음 세션 식별자")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("deviceId").type(STRING).description("사회자 기기 ID"),
                        fieldWithPath("deviceToken").type(STRING).description("사회자 기기 업로드 토큰")
                );

        @Test
        void 다기기_녹음_사회자_기기_재참가_성공() {
            doReturn(new RecordingDeviceResponseV2("hOsT02", DEVICE_TOKEN))
                    .when(recordingServiceV2).rejoinHost(any(), eq(SESSION_KEY));

            var document = document("recording-v2/rejoin-host", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().post("/api/v2/recordings/{sessionKey}/host-device", SESSION_KEY)
                    .then().statusCode(201);
        }
    }
}
