package com.debatetimer.controller.transcript;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.SegmentRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.UtteranceRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponses;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;

public class TranscriptDocumentTest extends BaseDocumentTest {

    private static final String REPORT_JSON = """
            {
              "prosArguments": [{"claim": "AI 기록은 공정성을 높인다",
                                 "evidences": [{"type": "LOGICAL", "content": "발언 누락을 막는다", "lineIndex": 0}]}],
              "consArguments": [{"claim": "인식 오류가 판정을 왜곡한다",
                                 "evidences": [{"type": "VAGUE", "content": "고유명사를 잘못 인식한다", "lineIndex": 1}]}],
              "clashes": [{"title": "기록 정확도는 충분한가?", "prosPosition": "보정으로 충분하다",
                           "consPosition": "오류가 남는다", "advantage": "PROS", "reason": "구체적 사례를 들었다", "lineIndices": [0, 1]}],
              "keyMoments": [{"lineIndex": 1, "label": "흐름 전환", "reason": "반대 측이 쟁점을 정확도로 옮겼다"}],
              "teamFeedback": {
                "pros": {"strengths": ["근거가 구체적이다"], "weaknesses": ["반론 대응이 짧다"]},
                "cons": {"strengths": ["쟁점을 잘 짚었다"], "weaknesses": ["근거가 부족하다"]}
              }
            }
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode report() {
        try {
            return objectMapper.readTree(REPORT_JSON);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Nested
    class CreateTranscript {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.TRANSCRIPT_API)
                .summary(featureFlagSummary(FeatureFlag.TRANSCRIPT, "속기록 저장"))
                .description(featureFlagDescription(FeatureFlag.TRANSCRIPT) + """
                        토론이 끝난 뒤 타임박스별로 인식한 발언을 모아 속기록으로 저장합니다.
                        저장 직후 응답하며, AI 문장 보정과 분석 보고서 생성은 서버에서 비동기로 진행됩니다.
                        진행 상황은 속기록 상세 조회의 `status`, `refining` 으로 확인합니다.

                        - 시간표 이름 · 주제 · 팀 이름은 저장 시점 값을 스냅샷으로 남기므로, 이후 시간표를 수정해도 속기록은 바뀌지 않습니다.
                        - `sttMode` 를 보내지 않으면 `RPC` 로 저장합니다. (`RPC`: 크롬 실시간 음성 인식, `SONIOX`: 발언 구간 오디오 일괄 전사)
                        - 발언이 하나도 없으면 `EMPTY_TRANSCRIPT` 로 거절합니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("tableId").description("속기록을 만들 시간표 ID")
                )
                .requestBodyField(
                        fieldWithPath("segments").type(ARRAY).description("타임박스별 발언 구간 목록"),
                        fieldWithPath("segments[].timeBoxIndex").type(NUMBER).description("시간표 내 타임박스 순서 (0부터)"),
                        fieldWithPath("segments[].team").type(STRING).description("자유 토론 타임박스에서 발언한 팀 (PROS, CONS)").optional(),
                        fieldWithPath("segments[].utterances").type(ARRAY).description("구간에서 인식한 문장 목록"),
                        fieldWithPath("segments[].utterances[].offsetMs").type(NUMBER).description("구간 시작부터의 경과 시간(ms)"),
                        fieldWithPath("segments[].utterances[].text").type(STRING).description("문장"),
                        fieldWithPath("sttMode").type(STRING).description("음성 인식 방식 (RPC, SONIOX). 기본값 RPC").optional()
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("저장한 속기록 ID")
                );

        private final TranscriptCreateRequest request = new TranscriptCreateRequest(List.of(
                new SegmentRequest(0, null, List.of(
                        new UtteranceRequest(300L, "찬성 측 입론을 시작하겠습니다."),
                        new UtteranceRequest(4_200L, "AI 음성 기록은 토론 대회의 공정성을 높입니다.")
                )),
                new SegmentRequest(2, Stance.CONS, List.of(
                        new UtteranceRequest(1_000L, "인식 오류가 판정을 왜곡할 수 있습니다.")
                ))
        ), SttMode.SONIOX);

        @Test
        void 속기록_저장_성공() {
            doReturn(new TranscriptCreateResponse(7L)).when(transcriptService)
                    .create(eq(5L), eq(EXIST_MEMBER), any());

            var document = document("transcript/post", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", 5L)
                    .body(request)
                    .when().post("/api/transcripts/{tableId}")
                    .then().statusCode(201);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {"TABLE_NOT_FOUND", "EMPTY_TRANSCRIPT"}
        )
        @ParameterizedTest
        void 속기록_저장_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(transcriptService)
                    .create(anyLong(), any(), any());

            var document = document("transcript/post", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", 5L)
                    .body(request)
                    .when().post("/api/transcripts/{tableId}")
                    .then().statusCode(errorCode.getStatus().value());
        }

        @Test
        void 속기록_저장_권한_없음() {
            doThrow(new DTClientErrorException(ClientErrorCode.FEATURE_NOT_ALLOWED)).when(authService)
                    .authorizeFeatureFlag(any(), eq(FeatureFlag.TRANSCRIPT));

            var document = document("transcript/post", ClientErrorCode.FEATURE_NOT_ALLOWED)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("tableId", 5L)
                    .body(request)
                    .when().post("/api/transcripts/{tableId}")
                    .then().statusCode(403);
        }
    }

    @Nested
    class GetTranscripts {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.TRANSCRIPT_API)
                .summary(featureFlagSummary(FeatureFlag.TRANSCRIPT, "토론 기록 목록 조회"))
                .description(featureFlagDescription(FeatureFlag.TRANSCRIPT) + """
                        로그인한 회원의 속기록 목록을 최신순으로 조회합니다. 속기록 본문과 분석 보고서는 포함하지 않습니다.

                        - `tableName`, `agenda` 는 속기록 저장 시점 값입니다. 스냅샷 도입 전에 지워진 시간표의 기록은 null 입니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("transcripts").type(ARRAY).description("속기록 목록 (최신순)"),
                        fieldWithPath("transcripts[].id").type(NUMBER).description("속기록 ID"),
                        fieldWithPath("transcripts[].tableId").type(NUMBER).description("시간표 ID"),
                        fieldWithPath("transcripts[].tableName").type(STRING).description("저장 시점 시간표 이름").optional(),
                        fieldWithPath("transcripts[].agenda").type(STRING).description("저장 시점 토론 주제").optional(),
                        fieldWithPath("transcripts[].sttMode").type(STRING).description("음성 인식 방식 (RPC, SONIOX)"),
                        fieldWithPath("transcripts[].status").type(STRING).description("분석 상태 (PROCESSING, DONE, FAILED)"),
                        fieldWithPath("transcripts[].refining").type(BOOLEAN).description("AI 문장 보정 진행 중 여부"),
                        fieldWithPath("transcripts[].createdAt").type(STRING).description("저장 시각")
                );

        @Test
        void 토론_기록_목록_조회_성공() {
            TranscriptSummaryResponses response = new TranscriptSummaryResponses(List.of(
                    new TranscriptSummaryResponse(7L, 5L, "교내 토론 대회 결승", "AI 음성 기록을 도입해야 한다",
                            SttMode.SONIOX, TranscriptStatus.DONE, false, LocalDateTime.of(2026, 11, 8, 14, 30)),
                    new TranscriptSummaryResponse(6L, 5L, "교내 토론 대회 예선", "AI 음성 기록을 도입해야 한다",
                            SttMode.RPC, TranscriptStatus.PROCESSING, true, LocalDateTime.of(2026, 11, 7, 19, 0))
            ));
            doReturn(response).when(transcriptService).findAll(EXIST_MEMBER);

            var document = document("transcript/get-all", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().get("/api/transcripts")
                    .then().statusCode(200);
        }

        @Test
        void 토론_기록_목록_조회_권한_없음() {
            doThrow(new DTClientErrorException(ClientErrorCode.FEATURE_NOT_ALLOWED)).when(authService)
                    .authorizeFeatureFlag(any(), eq(FeatureFlag.TRANSCRIPT));

            var document = document("transcript/get-all", ClientErrorCode.FEATURE_NOT_ALLOWED)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .when().get("/api/transcripts")
                    .then().statusCode(403);
        }
    }

    @Nested
    class GetTranscript {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.TRANSCRIPT_API)
                .summary(featureFlagSummary(FeatureFlag.TRANSCRIPT, "속기록 상세 조회"))
                .description(featureFlagDescription(FeatureFlag.TRANSCRIPT) + """
                        속기록 본문과 분석 보고서를 조회합니다. 본인의 속기록만 조회할 수 있습니다.

                        ### 진행 상태
                        | 필드 | 값 | 의미 |
                        | :---: | :---: | --- |
                        | refining | true | AI 문장 보정 중. 끝나면 `lines[].text` 가 보정된 문장으로 바뀝니다. |
                        | status | PROCESSING | 분석 보고서 생성 중. `report` 는 null 입니다. |
                        | status | DONE | 분석 완료. `report` 에 보고서가 있습니다. |
                        | status | FAILED | 분석 실패. `errorMessage` 에 사유가 있고 `report` 는 null 입니다. |

                        ### report 구조
                        | 필드 | 설명 |
                        | --- | --- |
                        | prosArguments[], consArguments[] | 팀별 주장. `claim`, `evidences[]` (`type`(SOURCED, LOGICAL, VAGUE, NONE), `content`, `lineIndex`) |
                        | clashes[] | 쟁점. `title`, `prosPosition`, `consPosition`, `advantage`(PROS, CONS, EVEN), `reason`, `lineIndices[]` |
                        | keyMoments[] | 결정적 순간. `lineIndex`, `label`, `reason` |
                        | teamFeedback.pros, teamFeedback.cons | 팀별 피드백. `strengths[]`, `weaknesses[]` |

                        `lineIndex`, `lineIndices` 는 `lines[].index` 를 가리킵니다.
                        """)
                .requestHeader(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
                )
                .pathParameter(
                        parameterWithName("transcriptId").description("속기록 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("속기록 ID"),
                        fieldWithPath("tableId").type(NUMBER).description("시간표 ID"),
                        fieldWithPath("tableName").type(STRING).description("저장 시점 시간표 이름").optional(),
                        fieldWithPath("agenda").type(STRING).description("저장 시점 토론 주제").optional(),
                        fieldWithPath("prosTeamName").type(STRING).description("저장 시점 찬성팀 이름").optional(),
                        fieldWithPath("consTeamName").type(STRING).description("저장 시점 반대팀 이름").optional(),
                        fieldWithPath("status").type(STRING).description("분석 상태 (PROCESSING, DONE, FAILED)"),
                        fieldWithPath("sttMode").type(STRING).description("음성 인식 방식 (RPC, SONIOX)"),
                        fieldWithPath("refining").type(BOOLEAN).description("AI 문장 보정 진행 중 여부"),
                        fieldWithPath("lines").type(ARRAY).description("속기록 문장 목록 (발언 순)"),
                        fieldWithPath("lines[].index").type(NUMBER).description("문장 순서 (0부터)"),
                        fieldWithPath("lines[].timeBoxIndex").type(NUMBER).description("타임박스 순서"),
                        fieldWithPath("lines[].speechType").type(STRING).description("발언 유형"),
                        fieldWithPath("lines[].stance").type(STRING).description("발언 진영 (PROS, CONS, NEUTRAL)"),
                        fieldWithPath("lines[].speaker").type(STRING).description("발언자 이름").optional(),
                        fieldWithPath("lines[].offsetMs").type(NUMBER).description("타임박스 시작부터의 경과 시간(ms)"),
                        fieldWithPath("lines[].text").type(STRING).description("문장"),
                        subsectionWithPath("report").type(OBJECT).description("분석 보고서. status 가 DONE 일 때만 있음 (구조는 설명 참고)").optional(),
                        fieldWithPath("errorMessage").type(STRING).description("분석 실패 사유. status 가 FAILED 일 때만 있음").optional(),
                        fieldWithPath("shareToken").type(STRING).description("공유 링크 토큰. 공유하지 않으면 null").optional()
                );

        @Test
        void 속기록_상세_조회_성공() {
            TranscriptResponse response = new TranscriptResponse(7L, 5L, "교내 토론 대회 결승",
                    "AI 음성 기록을 도입해야 한다", "찬성", "반대", TranscriptStatus.DONE, SttMode.SONIOX, false,
                    List.of(
                            new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 300L,
                                    "AI 음성 기록은 토론 대회의 공정성을 높입니다."),
                            new TranscriptLine(1, 1, "입론", Stance.CONS, "비토", 1_000L,
                                    "인식 오류가 판정을 왜곡할 수 있습니다.")
                    ),
                    report(), null, "3f2b8c1e-7d4a-4e9b-a6c5-0d1e2f3a4b5c");
            doReturn(response).when(transcriptService).get(7L, EXIST_MEMBER);

            var document = document("transcript/get", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("transcriptId", 7L)
                    .when().get("/api/transcripts/{transcriptId}")
                    .then().statusCode(200);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {"TRANSCRIPT_NOT_FOUND"}
        )
        @ParameterizedTest
        void 속기록_상세_조회_실패(ClientErrorCode errorCode) {
            doThrow(new DTClientErrorException(errorCode)).when(transcriptService).get(anyLong(), any());

            var document = document("transcript/get", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("transcriptId", 7L)
                    .when().get("/api/transcripts/{transcriptId}")
                    .then().statusCode(errorCode.getStatus().value());
        }

        @Test
        void 속기록_상세_조회_권한_없음() {
            doThrow(new DTClientErrorException(ClientErrorCode.FEATURE_NOT_ALLOWED)).when(authService)
                    .authorizeFeatureFlag(any(), eq(FeatureFlag.TRANSCRIPT));

            var document = document("transcript/get", ClientErrorCode.FEATURE_NOT_ALLOWED)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .headers(EXIST_MEMBER_HEADER)
                    .pathParam("transcriptId", 7L)
                    .when().get("/api/transcripts/{transcriptId}")
                    .then().statusCode(403);
        }
    }
}
