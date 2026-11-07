package com.debatetimer.controller.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.SegmentRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.UtteranceRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptSummaryResponses;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.event.transcript.TranscriptAnalysisListener;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class TranscriptControllerTest extends BaseControllerTest {

    @MockitoBean
    private OpenAiClient openAiClient;

    // 속기록 저장 뒤 비동기로 도는 보정·분석이 다음 테스트의 같은 ID 속기록 행을 덮어쓰지 않도록 막는다
    @MockitoBean
    private TranscriptAnalysisListener transcriptAnalysisListener;

    private Member generateTranscriptMember(String email) {
        Member member = memberGenerator.generate(email);
        memberFeatureFlagEntityGenerator.generate(member, FeatureFlag.TRANSCRIPT);
        return member;
    }

    private TranscriptCreateResponse createTranscript(Member member, CustomizeTableEntity table) {
        TranscriptCreateRequest request = new TranscriptCreateRequest(List.of(
                new SegmentRequest(0, null, List.of(new UtteranceRequest(1_000L, "찬성 입론입니다.")))
        ));
        return given()
                .contentType(ContentType.JSON)
                .headers(headerGenerator.generateAccessTokenHeader(member))
                .pathParam("tableId", table.getId())
                .body(request)
                .when().post("/api/transcripts/{tableId}")
                .then().statusCode(201)
                .extract().as(TranscriptCreateResponse.class);
    }

    @Nested
    class CreateTranscript {

        @Test
        void 속기록을_저장한다() {
            Member bito = generateTranscriptMember("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);

            TranscriptCreateResponse response = createTranscript(bito, table);

            assertThat(response.id()).isPositive();
        }

        @Test
        void 속기록_플래그가_없으면_저장할_수_없다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            TranscriptCreateRequest request = new TranscriptCreateRequest(List.of(
                    new SegmentRequest(0, null, List.of(new UtteranceRequest(1_000L, "찬성 입론입니다.")))
            ));

            given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .pathParam("tableId", table.getId())
                    .body(request)
                    .when().post("/api/transcripts/{tableId}")
                    .then().statusCode(403);
        }

        @Test
        void 기록된_발언이_없으면_예외가_발생한다() {
            Member bito = generateTranscriptMember("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            TranscriptCreateRequest request = new TranscriptCreateRequest(List.of());

            given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .pathParam("tableId", table.getId())
                    .body(request)
                    .when().post("/api/transcripts/{tableId}")
                    .then().statusCode(400);
        }
    }

    @Nested
    class GetTranscript {

        @Test
        void 자신의_속기록을_조회한다() {
            Member bito = generateTranscriptMember("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            long transcriptId = createTranscript(bito, table).id();

            TranscriptResponse response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .pathParam("transcriptId", transcriptId)
                    .when().get("/api/transcripts/{transcriptId}")
                    .then().statusCode(200)
                    .extract().as(TranscriptResponse.class);

            assertAll(
                    () -> assertThat(response.tableId()).isEqualTo(table.getId()),
                    () -> assertThat(response.lines()).extracting("text").containsExactly("찬성 입론입니다.")
            );
        }

        @Test
        void 다른_회원의_속기록은_조회할_수_없다() {
            Member bito = generateTranscriptMember("default@gmail.com");
            Member other = generateTranscriptMember("other@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            long transcriptId = createTranscript(bito, table).id();
            Headers otherHeaders = headerGenerator.generateAccessTokenHeader(other);

            given()
                    .headers(otherHeaders)
                    .pathParam("transcriptId", transcriptId)
                    .when().get("/api/transcripts/{transcriptId}")
                    .then().statusCode(404);
        }
    }

    @Nested
    class GetTranscripts {

        @Test
        void 자신의_토론_기록_목록을_조회한다() {
            Member bito = generateTranscriptMember("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            long transcriptId = createTranscript(bito, table).id();

            TranscriptSummaryResponses response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .when().get("/api/transcripts")
                    .then().statusCode(200)
                    .extract().as(TranscriptSummaryResponses.class);

            assertAll(
                    () -> assertThat(response.transcripts()).hasSize(1),
                    () -> assertThat(response.transcripts().get(0).id()).isEqualTo(transcriptId),
                    () -> assertThat(response.transcripts().get(0).tableName()).isEqualTo(table.getName())
            );
        }

        @Test
        void 속기록_플래그가_없으면_조회할_수_없다() {
            Member bito = memberGenerator.generate("default@gmail.com");

            given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .when().get("/api/transcripts")
                    .then().statusCode(403);
        }

        @Test
        void 로그인하지_않으면_조회할_수_없다() {
            given()
                    .when().get("/api/transcripts")
                    .then().statusCode(401);
        }
    }
}
