package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.sharing.ActiveSharing;
import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventData;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.sharing.SharingLogEntity;
import com.debatetimer.event.sharing.SharingFinishedEvent;
import com.debatetimer.repository.sharing.SharingLogRepository;
import com.debatetimer.service.BaseServiceTest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

class SharingLogServiceTest extends BaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-07T10:00:00Z");
    private static final int TIME_BOX_COUNT = 3;

    @Autowired
    private SharingLogRepository sharingLogRepository;

    @Autowired
    private CustomizeTableDomainRepository customizeTableDomainRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private MutableClock clock;
    private ApplicationEventPublisher eventPublisher;
    private SharingLogService sharingLogService;
    private Member member;
    private long roomId;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        eventPublisher = mock(ApplicationEventPublisher.class);
        sharingLogService = transactional(new SharingLogService(
                sharingLogRepository, customizeTableDomainRepository, eventPublisher, clock));

        member = memberGenerator.generate("chairman@email.com");
        CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
        for (int sequence = 1; sequence <= TIME_BOX_COUNT; sequence++) {
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, sequence);
        }
        roomId = table.getId();
    }

    // 시각을 조정하기 위해 직접 만든 서비스에도 트랜잭션을 적용한다
    private SharingLogService transactional(SharingLogService target) {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager, new AnnotationTransactionAttributeSource()));
        return (SharingLogService) proxyFactory.getProxy();
    }

    private SharingLogEntity onlyLog() {
        List<SharingLogEntity> logs = sharingLogRepository.findAll();
        assertThat(logs).hasSize(1);
        return logs.get(0);
    }

    private TimerEvent event(TimerEventType eventType, int sequence) {
        TimerEventData data = new TimerEventData(CustomizeBoxType.NORMAL, sequence, null, 30, true, null, null);
        return new TimerEvent(eventType, data);
    }

    private LocalDateTime at(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Nested
    class Start {

        @Test
        void 공유를_시작하면_진행_중인_기록을_만든다() {
            sharingLogService.start(roomId);

            SharingLogEntity log = onlyLog();
            assertAll(
                    () -> assertThat(log.getTableId()).isEqualTo(roomId),
                    () -> assertThat(log.getMemberId()).isEqualTo(member.getId()),
                    () -> assertThat(log.getStartedAt()).isEqualTo(at(NOW)),
                    () -> assertThat(log.getEndedAt()).isNull(),
                    () -> assertThat(log.getAudienceCount()).isZero(),
                    () -> assertThat(log.getStatus()).isEqualTo(SharingLogStatus.SHARING)
            );
        }

        @Test
        void 진행_중인_공유를_다시_시작하면_같은_기록을_쓴다() {
            sharingLogService.start(roomId);

            sharingLogService.start(roomId);

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.SHARING);
        }

        @Test
        void 연결이_끊긴_뒤_유예_시간_안에_다시_시작하면_같은_기록을_쓴다() {
            sharingLogService.start(roomId);
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE.minusSeconds(1));

            sharingLogService.start(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE);
            sharingLogService.closeExpired();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.SHARING);
        }

        @Test
        void 연결이_끊긴_뒤_유예_시간이_지나_다시_시작하면_이전_기록을_정리하고_새_기록을_만든다() {
            sharingLogService.start(roomId);
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE);

            sharingLogService.start(roomId);

            List<SharingLogEntity> logs = sharingLogRepository.findAll();
            assertThat(logs).extracting(SharingLogEntity::getStatus)
                    .containsExactly(SharingLogStatus.ABANDONED, SharingLogStatus.SHARING);
        }
    }

    @Nested
    class Finish {

        @Test
        void 종료하면_종료_시각과_함께_기록을_종료하고_종료_이벤트를_발행한다() {
            sharingLogService.start(roomId);
            sharingLogService.joinAudience(roomId);
            sharingLogService.joinAudience(roomId);
            clock.advance(Duration.ofMinutes(30));

            sharingLogService.finish(roomId);

            ArgumentCaptor<SharingFinishedEvent> captor = ArgumentCaptor.forClass(SharingFinishedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            SharingFinishedEvent event = captor.getValue();
            SharingLogEntity log = onlyLog();
            assertAll(
                    () -> assertThat(log.getStatus()).isEqualTo(SharingLogStatus.FINISHED),
                    () -> assertThat(log.getEndedAt()).isEqualTo(at(NOW.plus(Duration.ofMinutes(30)))),
                    () -> assertThat(event.memberEmail()).isEqualTo("chairman@email.com"),
                    () -> assertThat(event.tableId()).isEqualTo(roomId),
                    () -> assertThat(event.durationSeconds()).isEqualTo(30 * 60),
                    () -> assertThat(event.audienceCount()).isEqualTo(2)
            );
        }

        @Test
        void 종료한_뒤_다시_시작하면_새_기록을_만든다() {
            sharingLogService.start(roomId);
            sharingLogService.finish(roomId);

            sharingLogService.start(roomId);

            assertThat(sharingLogRepository.findAll()).extracting(SharingLogEntity::getStatus)
                    .containsExactly(SharingLogStatus.FINISHED, SharingLogStatus.SHARING);
        }

        @Test
        void 진행_중인_공유가_없으면_종료_이벤트를_발행하지_않는다() {
            sharingLogService.finish(roomId);

            verify(eventPublisher, never()).publishEvent(any(Object.class));
        }
    }

    @Nested
    class JoinAudience {

        @Test
        void 청중이_입장할_때마다_청중_수를_늘린다() {
            sharingLogService.start(roomId);

            sharingLogService.joinAudience(roomId);
            sharingLogService.joinAudience(roomId);
            sharingLogService.joinAudience(roomId);

            assertThat(onlyLog().getAudienceCount()).isEqualTo(3);
        }

        @Test
        void 진행_중인_공유가_없으면_기록하지_않는다() {
            sharingLogService.joinAudience(roomId);

            assertThat(sharingLogRepository.findAll()).isEmpty();
        }
    }

    @Nested
    class CloseExpired {

        @Test
        void 마지막_타임박스에서_연결이_끊긴_뒤_돌아오지_않으면_끊긴_시각으로_종료하고_종료_이벤트를_발행한다() {
            sharingLogService.start(roomId);
            sharingLogService.joinAudience(roomId);
            sharingLogService.recordEvent(roomId, event(TimerEventType.PLAY, TIME_BOX_COUNT - 1));
            clock.advance(Duration.ofMinutes(20));
            Instant disconnectedAt = clock.instant();
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE);

            sharingLogService.closeExpired();

            ArgumentCaptor<SharingFinishedEvent> captor = ArgumentCaptor.forClass(SharingFinishedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            SharingFinishedEvent event = captor.getValue();
            SharingLogEntity log = onlyLog();
            assertAll(
                    () -> assertThat(log.getStatus()).isEqualTo(SharingLogStatus.FINISHED),
                    () -> assertThat(log.getEndedAt()).isEqualTo(at(disconnectedAt)),
                    () -> assertThat(event.sharingLogId()).isEqualTo(log.getId()),
                    () -> assertThat(event.memberEmail()).isEqualTo("chairman@email.com"),
                    () -> assertThat(event.tableId()).isEqualTo(roomId),
                    () -> assertThat(event.durationSeconds()).isEqualTo(20 * 60),
                    () -> assertThat(event.audienceCount()).isEqualTo(1)
            );
        }

        @Test
        void 다음_이벤트는_이동한_뒤_순서로_마지막_타임박스를_판단한다() {
            sharingLogService.start(roomId);
            sharingLogService.recordEvent(roomId, event(TimerEventType.NEXT, TIME_BOX_COUNT - 2));
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE);

            sharingLogService.closeExpired();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.FINISHED);
        }

        @Test
        void 이전_이벤트는_이동한_뒤_순서로_마지막_타임박스를_판단한다() {
            sharingLogService.start(roomId);
            sharingLogService.recordEvent(roomId, event(TimerEventType.BEFORE, TIME_BOX_COUNT - 1));
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE);

            sharingLogService.closeExpired();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.ABANDONED);
        }

        @Test
        void 마지막_타임박스_전에_연결이_끊긴_뒤_돌아오지_않으면_중단으로_기록하고_종료_이벤트를_발행하지_않는다() {
            sharingLogService.start(roomId);
            sharingLogService.recordEvent(roomId, event(TimerEventType.PLAY, 0));
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE);

            sharingLogService.closeExpired();

            assertAll(
                    () -> assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.ABANDONED),
                    () -> verify(eventPublisher, never()).publishEvent(any(Object.class))
            );
        }

        @Test
        void 유예_시간이_지나지_않았으면_정리하지_않는다() {
            sharingLogService.start(roomId);
            sharingLogService.disconnect(roomId);
            clock.advance(ActiveSharing.RECONNECT_GRACE.minusSeconds(1));

            sharingLogService.closeExpired();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.SHARING);
        }

        @Test
        void 연결이_유지돼도_너무_오래_이어진_공유는_정리한다() {
            sharingLogService.start(roomId);
            clock.advance(ActiveSharing.STALE_THRESHOLD);

            sharingLogService.closeExpired();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.ABANDONED);
        }

        @Test
        void 추적하지_못하게_된_오래된_기록은_정리하지_않는다() {
            LocalDateTime startedAt = at(NOW.minus(ActiveSharing.STALE_THRESHOLD).minusSeconds(1));
            sharingLogRepository.save(new SharingLogEntity(roomId, member.getId(), startedAt));

            sharingLogService.closeExpired();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.SHARING);
        }
    }

    @Nested
    class AbandonUntracked {

        @Test
        void 추적하지_못하게_된_오래된_기록은_중단으로_정리한다() {
            LocalDateTime startedAt = at(NOW.minus(ActiveSharing.STALE_THRESHOLD).minusSeconds(1));
            sharingLogRepository.save(new SharingLogEntity(roomId, member.getId(), startedAt));

            sharingLogService.abandonUntracked();

            SharingLogEntity log = onlyLog();
            assertAll(
                    () -> assertThat(log.getStatus()).isEqualTo(SharingLogStatus.ABANDONED),
                    () -> assertThat(log.getEndedAt()).isNotNull()
            );
        }

        @Test
        void 오래되지_않은_기록은_정리하지_않는다() {
            LocalDateTime startedAt = at(NOW.minus(ActiveSharing.STALE_THRESHOLD).plusSeconds(1));
            sharingLogRepository.save(new SharingLogEntity(roomId, member.getId(), startedAt));

            sharingLogService.abandonUntracked();

            assertThat(onlyLog().getStatus()).isEqualTo(SharingLogStatus.SHARING);
        }
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
