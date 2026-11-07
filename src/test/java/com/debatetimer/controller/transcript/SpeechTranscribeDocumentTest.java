package com.debatetimer.controller.transcript;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.dto.transcript.response.SpeechTranscribeResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;

public class SpeechTranscribeDocumentTest extends BaseDocumentTest {

    private static final byte[] AUDIO = new byte[]{1, 2, 3};
    private static final String SESSION_ID = "3f2b8c1e-5d4a-4e7b-9c0d-1a2b3c4d5e6f";

    @Nested
    class TranscribeWithSoniox {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.STT_API)
                .summary("발언 구간 오디오 Soniox 전사")
                .description("""
                        토론 기록 Soniox 모드에서 타이머가 끝난 발언 구간 오디오를 받아 문장 단위로 전사합니다.
                        Soniox 전사가 끝날 때까지 기다린 뒤 응답하므로 최대 3분 정도 걸릴 수 있습니다.

                        ### multipart/form-data 파트
                        | 파트 | 필수 | 설명 |
                        | :---: | :---: | --- |
                        | audio | O | 발언 구간 오디오 파일 (webm, mp4 등, 최대 30MB) |
                        | sessionId | X | 녹음 세션 식별자. 토론 기록을 켤 때마다 프론트에서 새로 만듭니다. 보내면 오디오를 S3 `stt-audio/{memberId}/{sessionId}/` 아래에 보관하고, 영문·숫자·'_'·'-'로 된 64자 이하가 아니면 보관만 건너뜁니다. |
                        | language | X | 화면 언어 (ko-KR, en-US 등). en으로 시작하면 영어, 그 외에는 한국어로 전사합니다. |

                        오디오 보관은 전사 응답과 별개로 처리되며, 보관에 실패해도 전사 결과에는 영향이 없습니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .requestPart(
                        partWithName("audio").description("발언 구간 오디오 파일"),
                        partWithName("sessionId").optional().description("녹음 세션 식별자"),
                        partWithName("language").optional().description("화면 언어 (기본값 한국어)")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("utterances").type(ARRAY).description("전사한 문장 목록"),
                        fieldWithPath("utterances[].offsetMs").type(NUMBER).description("구간 오디오 시작 시점부터의 경과 시간(ms)"),
                        fieldWithPath("utterances[].text").type(STRING).description("문장")
                );

        @Test
        void 발언_구간_오디오_전사_성공() {
            SpeechTranscribeResponse response = new SpeechTranscribeResponse(List.of(
                    new Utterance(300, "찬성 측 입론을 시작하겠습니다."),
                    new Utterance(4_200, "AI 음성 기록은 토론 대회의 공정성을 높입니다.")
            ));
            doReturn(response).when(speechTranscribeService)
                    .transcribeWithSoniox(eq(EXIST_MEMBER_ID), any(), eq(SESSION_ID), eq("ko-KR"));

            var document = document("stt/soniox", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .multiPart("audio", "segment.webm", AUDIO, "audio/webm")
                    .multiPart("sessionId", SESSION_ID)
                    .multiPart("language", "ko-KR")
                    .when().post("/api/stt/soniox")
                    .then().statusCode(200);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {"EMPTY_AUDIO", "STT_TOO_MANY_REQUESTS"}
        )
        @ParameterizedTest
        void 발언_구간_오디오_전사_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(speechTranscribeService)
                    .transcribeWithSoniox(anyLong(), any(), any(), any());

            var document = document("stt/soniox", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .multiPart("audio", "segment.webm", AUDIO, "audio/webm")
                    .multiPart("sessionId", SESSION_ID)
                    .multiPart("language", "ko-KR")
                    .when().post("/api/stt/soniox")
                    .then().statusCode(errorCode.getStatus().value());
        }

        @Test
        void 발언_구간_오디오_전사_권한_없음() {
            doThrow(new DTClientErrorException(ClientErrorCode.FEATURE_NOT_ALLOWED)).when(authService)
                    .authorizeFeatureFlag(any(), eq(FeatureFlag.TRANSCRIPT));

            var document = document("stt/soniox", ClientErrorCode.FEATURE_NOT_ALLOWED)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .multiPart("audio", "segment.webm", AUDIO, "audio/webm")
                    .when().post("/api/stt/soniox")
                    .then().statusCode(403);
        }
    }
}
