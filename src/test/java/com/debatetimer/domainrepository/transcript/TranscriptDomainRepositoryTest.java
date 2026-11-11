package com.debatetimer.domainrepository.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.Transcript;
import com.debatetimer.domain.transcript.TranscriptLine;
import com.debatetimer.domain.transcript.TranscriptStatus;
import com.debatetimer.domain.transcript.TranscriptTableSnapshot;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.transcript.TranscriptEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TranscriptDomainRepositoryTest extends BaseDomainRepositoryTest {

    private static final List<TranscriptLine> LINES = List.of(
            new TranscriptLine(0, 0, "입론", Stance.PROS, "1번", 1_000L, "찬성 입론입니다."),
            new TranscriptLine(1, 1, "자유토론", Stance.CONS, null, 65_000L, "반대 측 반박입니다.")
    );

    private static final TranscriptTableSnapshot SNAPSHOT = new TranscriptTableSnapshot("정기토론", "주제", "찬성", "반대");

    @Autowired
    private TranscriptDomainRepository transcriptDomainRepository;

    @Nested
    class Create {

        @Test
        void 속기록을_분석_중_상태로_저장한다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            Transcript found = transcriptDomainRepository.getById(created.id());
            assertAll(
                    () -> assertThat(found.tableId()).isEqualTo(1L),
                    () -> assertThat(found.memberId()).isEqualTo(2L),
                    () -> assertThat(found.status()).isEqualTo(TranscriptStatus.PROCESSING),
                    () -> assertThat(found.sttMode()).isEqualTo(SttMode.RPC),
                    () -> assertThat(found.table()).isEqualTo(SNAPSHOT),
                    () -> assertThat(found.refining()).isTrue(),
                    () -> assertThat(found.rawLines()).isEqualTo(LINES),
                    () -> assertThat(found.lines()).isEqualTo(LINES),
                    () -> assertThat(found.reportJson()).isNull()
            );
        }
    }

    @Nested
    class GetByIdAndMemberId {

        @Test
        void 다른_회원의_속기록이면_예외를_던진다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            assertThatThrownBy(() -> transcriptDomainRepository.getByIdAndMemberId(created.id(), 3L))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TRANSCRIPT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class Refine {

        @Test
        void 다듬은_속기록을_저장하고_원문은_보관한다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);
            List<TranscriptLine> refined = List.of(
                    new TranscriptLine(0, 0, "입론", Stance.PROS, "1번", 1_000L, "찬성 입론입니다."));

            transcriptDomainRepository.refine(created.id(), refined);

            Transcript found = transcriptDomainRepository.getById(created.id());
            assertAll(
                    () -> assertThat(found.refining()).isFalse(),
                    () -> assertThat(found.lines()).isEqualTo(refined),
                    () -> assertThat(found.rawLines()).isEqualTo(LINES),
                    () -> assertThat(found.status()).isEqualTo(TranscriptStatus.PROCESSING)
            );
        }

        @Test
        void 다듬기를_건너뛰면_원문을_속기록으로_쓴다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            transcriptDomainRepository.skipRefinement(created.id());

            Transcript found = transcriptDomainRepository.getById(created.id());
            assertAll(
                    () -> assertThat(found.refining()).isFalse(),
                    () -> assertThat(found.lines()).isEqualTo(LINES)
            );
        }
    }

    @Nested
    class Complete {

        @Test
        void 분석_보고서를_저장하고_완료_상태로_바꾼다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            transcriptDomainRepository.complete(created.id(), "{\"clashes\":[]}");

            Transcript found = transcriptDomainRepository.getById(created.id());
            assertAll(
                    () -> assertThat(found.status()).isEqualTo(TranscriptStatus.DONE),
                    () -> assertThat(found.reportJson()).isEqualTo("{\"clashes\":[]}")
            );
        }
    }

    @Nested
    class Fail {

        @Test
        void 오류_메시지를_최대_길이로_잘라_실패_상태로_바꾼다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            transcriptDomainRepository.fail(created.id(), "가".repeat(TranscriptEntity.ERROR_MESSAGE_MAX_LENGTH + 10));

            Transcript found = transcriptDomainRepository.getById(created.id());
            assertAll(
                    () -> assertThat(found.status()).isEqualTo(TranscriptStatus.FAILED),
                    () -> assertThat(found.errorMessage()).hasSize(TranscriptEntity.ERROR_MESSAGE_MAX_LENGTH)
            );
        }
    }

    @Nested
    class Share {

        @Test
        void 공유_토큰을_발급하고_토큰으로_조회할_수_있다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            Transcript shared = transcriptDomainRepository.share(created.id(), 2L);

            Transcript found = transcriptDomainRepository.getByShareToken(shared.shareToken());
            assertAll(
                    () -> assertThat(shared.shareToken()).isNotBlank(),
                    () -> assertThat(found.id()).isEqualTo(created.id())
            );
        }

        @Test
        void 이미_공유_중이면_기존_토큰을_돌려준다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);
            String firstToken = transcriptDomainRepository.share(created.id(), 2L).shareToken();

            String secondToken = transcriptDomainRepository.share(created.id(), 2L).shareToken();

            assertThat(secondToken).isEqualTo(firstToken);
        }

        @Test
        void 다른_회원의_속기록은_공유할_수_없다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);

            assertThatThrownBy(() -> transcriptDomainRepository.share(created.id(), 3L))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TRANSCRIPT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class Unshare {

        @Test
        void 공유를_해제하면_토큰으로_조회할_수_없다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);
            String token = transcriptDomainRepository.share(created.id(), 2L).shareToken();

            transcriptDomainRepository.unshare(created.id(), 2L);

            assertAll(
                    () -> assertThat(transcriptDomainRepository.getById(created.id()).shareToken()).isNull(),
                    () -> assertThatThrownBy(() -> transcriptDomainRepository.getByShareToken(token))
                            .isInstanceOf(DTClientErrorException.class)
                            .hasMessage(ClientErrorCode.TRANSCRIPT_NOT_FOUND.getMessage())
            );
        }

        @Test
        void 다시_공유하면_새_토큰을_발급한다() {
            Transcript created = transcriptDomainRepository.create(1L, 2L, SNAPSHOT, SttMode.RPC, LINES);
            String oldToken = transcriptDomainRepository.share(created.id(), 2L).shareToken();
            transcriptDomainRepository.unshare(created.id(), 2L);

            String newToken = transcriptDomainRepository.share(created.id(), 2L).shareToken();

            assertThat(newToken).isNotEqualTo(oldToken);
        }
    }
}
