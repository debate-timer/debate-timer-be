package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.domain.transcript.TranscriptTableSnapshot;
import com.debatetimer.domainrepository.transcript.TranscriptDomainRepository;
import com.debatetimer.dto.admin.response.AdminTranscriptDetailResponse;
import com.debatetimer.dto.admin.response.AdminTranscriptPageResponse;
import com.debatetimer.dto.admin.response.AdminTranscriptResponse;
import com.debatetimer.dto.transcript.response.TranscriptCreateResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.event.transcript.TranscriptAnalysisListener;
import com.debatetimer.event.transcript.TranscriptCreatedEvent;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AdminTranscriptControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";
    private static final List<TranscriptLine> RAW_LINES = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 1_000, "찬송추 입론입니다")
    );
    private static final List<TranscriptLine> REFINED_LINES = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "콜리", 1_000, "찬성 측 입론입니다.")
    );

    @Autowired
    private TranscriptDomainRepository transcriptDomainRepository;

    // 재실행 뒤 비동기로 도는 보정·분석이 실제 OpenAI 를 부르거나 다음 테스트 행을 덮어쓰지 않도록 막는다
    @MockitoBean
    private TranscriptAnalysisListener transcriptAnalysisListener;

    private Transcript analyzedTranscript(Member member) {
        CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
        Transcript transcript = transcriptDomainRepository.create(table.getId(), member.getId(),
                TranscriptTableSnapshot.from(table.toDomain()), SttMode.RPC, RAW_LINES);
        transcriptDomainRepository.refine(transcript.id(), REFINED_LINES);
        transcriptDomainRepository.complete(transcript.id(), "{\"prosArguments\":[]}");
        return transcriptDomainRepository.getById(transcript.id());
    }

    @Nested
    class GetTranscripts {

        @Test
        void 모든_회원의_토론_기록을_최신순으로_조회한다() {
            Transcript first = analyzedTranscript(memberGenerator.generate("bito@gmail.com"));
            Transcript second = analyzedTranscript(memberGenerator.generate("coli@gmail.com"));

            AdminTranscriptPageResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/transcripts")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminTranscriptPageResponse.class);

            assertAll(
                    () -> assertThat(response.totalElements()).isEqualTo(2),
                    () -> assertThat(response.transcripts()).extracting(AdminTranscriptResponse::id)
                            .containsExactly(second.id(), first.id()),
                    () -> assertThat(response.transcripts()).extracting(AdminTranscriptResponse::memberEmail)
                            .containsExactly("coli@gmail.com", "bito@gmail.com")
            );
        }

        @Test
        void 페이지_크기가_범위를_벗어나면_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("size", 101)
                    .when().get("/api/admin/transcripts")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .when().get("/api/admin/transcripts")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class GetTranscript {

        @Test
        void 원문_보정본_분석_보고서를_함께_조회한다() {
            Transcript transcript = analyzedTranscript(memberGenerator.generate("bito@gmail.com"));

            AdminTranscriptDetailResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/transcripts/{transcriptId}", transcript.id())
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminTranscriptDetailResponse.class);

            assertAll(
                    () -> assertThat(response.rawLines()).isEqualTo(RAW_LINES),
                    () -> assertThat(response.lines()).isEqualTo(REFINED_LINES),
                    () -> assertThat(response.report().has("prosArguments")).isTrue(),
                    () -> assertThat(response.status()).isEqualTo(TranscriptStatus.DONE)
            );
        }

        @Test
        void 없는_토론_기록은_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/transcripts/{transcriptId}", 999)
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class Rerun {

        @Test
        void 같은_메타_정보와_STT_원문으로_새_기록을_만들고_보정_분석을_다시_돌린다() {
            Transcript source = analyzedTranscript(memberGenerator.generate("bito@gmail.com"));

            TranscriptCreateResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().post("/api/admin/transcripts/{transcriptId}/rerun", source.id())
                    .then().statusCode(HttpStatus.CREATED.value())
                    .extract().as(TranscriptCreateResponse.class);

            Transcript rerun = transcriptDomainRepository.getById(response.id());
            assertAll(
                    () -> assertThat(rerun.id()).isNotEqualTo(source.id()),
                    () -> assertThat(rerun.sourceTranscriptId()).isEqualTo(source.id()),
                    () -> assertThat(rerun.memberId()).isEqualTo(source.memberId()),
                    () -> assertThat(rerun.tableId()).isEqualTo(source.tableId()),
                    () -> assertThat(rerun.table()).isEqualTo(source.table()),
                    () -> assertThat(rerun.sttMode()).isEqualTo(source.sttMode()),
                    () -> assertThat(rerun.lines()).isEqualTo(RAW_LINES),
                    () -> assertThat(rerun.rawLines()).isEqualTo(RAW_LINES),
                    () -> assertThat(rerun.status()).isEqualTo(TranscriptStatus.PROCESSING),
                    () -> assertThat(rerun.reportJson()).isNull(),
                    () -> verify(transcriptAnalysisListener)
                            .handleTranscriptCreated(new TranscriptCreatedEvent(response.id()))
            );
        }

        @Test
        void 없는_토론_기록은_다시_실행할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().post("/api/admin/transcripts/{transcriptId}/rerun", 999)
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
