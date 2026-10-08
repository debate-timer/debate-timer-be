package com.debatetimer.domainrepository.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.sharing.SharingLog;
import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SharingLogDomainRepositoryTest extends BaseDomainRepositoryTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 10, 7, 10, 0, 0);

    @Autowired
    private SharingLogDomainRepository sharingLogDomainRepository;

    private SharingLog createSharing() {
        return sharingLogDomainRepository.create(new SharingLog(1L, 2L, STARTED_AT));
    }

    @Nested
    class Create {

        @Test
        void 공유_기록을_생성한다() {
            SharingLog created = createSharing();

            SharingLog found = sharingLogDomainRepository.getById(created.getId());
            assertAll(
                    () -> assertThat(found.getTableId()).isEqualTo(1L),
                    () -> assertThat(found.getMemberId()).isEqualTo(2L),
                    () -> assertThat(found.getStartedAt()).isEqualTo(STARTED_AT),
                    () -> assertThat(found.getStatus()).isEqualTo(SharingLogStatus.SHARING)
            );
        }
    }

    @Nested
    class GetById {

        @Test
        void 공유_기록이_없으면_예외를_던진다() {
            assertThatThrownBy(() -> sharingLogDomainRepository.getById(1L))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.SHARING_LOG_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class IncreaseAudienceCount {

        @Test
        void 청중_수를_늘린다() {
            SharingLog created = createSharing();

            sharingLogDomainRepository.increaseAudienceCount(created.getId());
            sharingLogDomainRepository.increaseAudienceCount(created.getId());

            assertThat(sharingLogDomainRepository.getById(created.getId()).getAudienceCount()).isEqualTo(2);
        }
    }

    @Nested
    class Finish {

        @Test
        void 진행_중인_기록을_종료하고_종료된_기록을_반환한다() {
            SharingLog created = createSharing();
            sharingLogDomainRepository.increaseAudienceCount(created.getId());
            LocalDateTime endedAt = STARTED_AT.plusMinutes(30);

            Optional<SharingLog> finished = sharingLogDomainRepository.finish(created.getId(), endedAt);

            assertThat(finished).hasValueSatisfying(sharingLog -> assertAll(
                    () -> assertThat(sharingLog.getStatus()).isEqualTo(SharingLogStatus.FINISHED),
                    () -> assertThat(sharingLog.getEndedAt()).isEqualTo(endedAt),
                    () -> assertThat(sharingLog.getDurationSeconds()).isEqualTo(30 * 60),
                    () -> assertThat(sharingLog.getAudienceCount()).isEqualTo(1)
            ));
        }

        @Test
        void 이미_끝난_기록은_바꾸지_않고_빈_값을_반환한다() {
            SharingLog created = createSharing();
            sharingLogDomainRepository.abandon(created.getId(), STARTED_AT.plusMinutes(5));

            Optional<SharingLog> finished = sharingLogDomainRepository.finish(created.getId(), STARTED_AT.plusMinutes(30));

            SharingLog found = sharingLogDomainRepository.getById(created.getId());
            assertAll(
                    () -> assertThat(finished).isEmpty(),
                    () -> assertThat(found.getStatus()).isEqualTo(SharingLogStatus.ABANDONED),
                    () -> assertThat(found.getEndedAt()).isEqualTo(STARTED_AT.plusMinutes(5))
            );
        }
    }

    @Nested
    class Abandon {

        @Test
        void 진행_중인_기록을_중단한다() {
            SharingLog created = createSharing();

            sharingLogDomainRepository.abandon(created.getId(), STARTED_AT.plusMinutes(5));

            SharingLog found = sharingLogDomainRepository.getById(created.getId());
            assertAll(
                    () -> assertThat(found.getStatus()).isEqualTo(SharingLogStatus.ABANDONED),
                    () -> assertThat(found.getEndedAt()).isEqualTo(STARTED_AT.plusMinutes(5))
            );
        }

        @Test
        void 이미_종료된_기록은_중단으로_바꾸지_않는다() {
            SharingLog created = createSharing();
            sharingLogDomainRepository.finish(created.getId(), STARTED_AT.plusMinutes(30));

            sharingLogDomainRepository.abandon(created.getId(), STARTED_AT.plusMinutes(40));

            assertThat(sharingLogDomainRepository.getById(created.getId()).getStatus())
                    .isEqualTo(SharingLogStatus.FINISHED);
        }
    }

    @Nested
    class FindAllFinishedStartedBetween {

        @Test
        void 기간_안에_시작해_종료된_기록만_찾는다() {
            SharingLog finished = createSharing();
            sharingLogDomainRepository.finish(finished.getId(), STARTED_AT.plusMinutes(30));
            SharingLog abandoned = createSharing();
            sharingLogDomainRepository.abandon(abandoned.getId(), STARTED_AT.plusMinutes(30));
            createSharing();

            assertAll(
                    () -> assertThat(sharingLogDomainRepository.findAllFinishedStartedBetween(
                            STARTED_AT, STARTED_AT.plusDays(1)))
                            .extracting(SharingLog::getId)
                            .containsExactly(finished.getId()),
                    () -> assertThat(sharingLogDomainRepository.findAllFinishedStartedBetween(
                            STARTED_AT.plusSeconds(1), STARTED_AT.plusDays(1)))
                            .isEmpty()
            );
        }
    }
}
