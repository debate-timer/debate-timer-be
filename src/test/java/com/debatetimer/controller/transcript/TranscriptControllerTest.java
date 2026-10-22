package com.debatetimer.controller.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.client.llm.OpenAiClient;
import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.SegmentRequest;
import com.debatetimer.dto.transcript.request.TranscriptCreateRequest.UtteranceRequest;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.dto.transcript.response.TranscriptResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class TranscriptControllerTest extends BaseControllerTest {

    @MockitoBean
    private OpenAiClient openAiClient;

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
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);

            TranscriptCreateResponse response = createTranscript(bito, table);

            assertThat(response.id()).isPositive();
        }

        @Test
        void 기록된_발언이_없으면_예외가_발생한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
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
            Member bito = memberGenerator.generate("default@gmail.com");
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
            Member bito = memberGenerator.generate("default@gmail.com");
            Member other = memberGenerator.generate("other@gmail.com");
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
}
