package com.debatetimer.service.sharing;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domain.sharing.TimerEventData;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.entity.sharing.SharingLogEntity;
import com.debatetimer.event.sharing.SharingFinishedEvent;
import com.debatetimer.repository.sharing.SharingLogRepository;
import jakarta.annotation.Nullable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 라이브 공유 한 번을 공유 기록 한 행으로 추적한다.
 * - 사회자가 공유를 시작하면 기록을 만들고, 사회자가 종료(FINISHED)를 발행할 때까지 같은 기록을 쓴다.
 * - 사회자 연결이 끊긴 뒤 재연결 유예 시간 안에 다시 공유를 시작하면 같은 기록을 이어서 쓴다.
 * - 유예 시간이 지나도록 돌아오지 않으면, 마지막 타임박스까지 진행했을 때는 종료로, 아니면 중단으로 기록한다.
 * - 종료로 기록할 때는 사회자가 직접 종료했는지와 관계없이 종료 이벤트를 발행한다.
 */
@Service
public class SharingLogService {

    static final Duration RECONNECT_GRACE = Duration.ofMinutes(1);
    static final Duration STALE_THRESHOLD = Duration.ofHours(12);

    private final Map<Long, ActiveSharing> activeSharings = new ConcurrentHashMap<>();
    private final SharingLogRepository sharingLogRepository;
    private final CustomizeTableDomainRepository customizeTableDomainRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public SharingLogService(
            SharingLogRepository sharingLogRepository,
            CustomizeTableDomainRepository customizeTableDomainRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this(sharingLogRepository, customizeTableDomainRepository, eventPublisher, Clock.systemDefaultZone());
    }

    SharingLogService(
            SharingLogRepository sharingLogRepository,
            CustomizeTableDomainRepository customizeTableDomainRepository,
            ApplicationEventPublisher eventPublisher,
            Clock clock
    ) {
        this.sharingLogRepository = sharingLogRepository;
        this.customizeTableDomainRepository = customizeTableDomainRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * 진행 중인 기록이 있으면(재연결·다른 기기로의 이전) 이어서 쓰고, 없으면 새 기록을 만든다.
     * 끊긴 지 유예 시간이 지난 기록은 정리한 뒤 새 기록을 만든다.
     */
    @Transactional
    public synchronized void start(long roomId) {
        Instant now = clock.instant();
        ActiveSharing active = activeSharings.get(roomId);
        if (active != null && !active.isReconnectExpired(now)) {
            activeSharings.put(roomId, active.reconnected());
            return;
        }
        if (active != null) {
            close(roomId, active, now);
        }

        CustomizeTable table = customizeTableDomainRepository.getById(roomId);
        int timeBoxCount = customizeTableDomainRepository.countTimeBoxes(roomId);
        SharingLogEntity sharingLog = sharingLogRepository.save(
                new SharingLogEntity(roomId, table.getMember().getId(), toLocalDateTime(now)));
        activeSharings.put(roomId, new ActiveSharing(
                sharingLog.getId(),
                table.getMember().getEmail(),
                table.getName(),
                timeBoxCount,
                now,
                null,
                null
        ));
    }

    /**
     * 끊긴 공유가 마지막 타임박스까지 진행됐는지 판단할 수 있도록, 청중 화면에 표시될 타임박스 순서를 기록한다.
     * 이전·다음 이벤트는 이동하기 전 순서를 담고 있어, 이동한 뒤 순서로 바꿔 기록한다.
     */
    public void recordEvent(long roomId, TimerEvent timerEvent) {
        TimerEventData data = timerEvent.getTimerEventData();
        if (data == null) {
            return;
        }
        int displayedSequence = toDisplayedSequence(timerEvent.getEventType(), data.getSequence());
        activeSharings.computeIfPresent(roomId, (id, active) -> active.withSequence(displayedSequence));
    }

    @Transactional
    public synchronized void finish(long roomId) {
        ActiveSharing active = activeSharings.remove(roomId);
        if (active == null) {
            return;
        }
        SharingLogEntity sharingLog = sharingLogRepository.findById(active.logId()).orElse(null);
        if (sharingLog == null) {
            return;
        }
        sharingLog.finish(toLocalDateTime(clock.instant()));
        publishFinished(active, sharingLog);
    }

    /**
     * 청중이 입장할 때마다 청중 수를 늘린다. 같은 청중의 재입장도 함께 센다.
     */
    @Transactional
    public void joinAudience(long roomId) {
        ActiveSharing active = activeSharings.get(roomId);
        if (active == null) {
            return;
        }
        sharingLogRepository.increaseAudienceCount(active.logId());
    }

    public synchronized void disconnect(long roomId) {
        Instant now = clock.instant();
        activeSharings.computeIfPresent(roomId, (id, active) -> active.disconnected(now));
    }

    /**
     * 재연결 유예 시간이 지난 공유와 너무 오래 이어진 공유를 정리한다.
     * 서버 재시작 등으로 추적하지 못하게 된 기록도 중단으로 정리한다.
     */
    @Transactional
    public synchronized void closeExpired() {
        Instant now = clock.instant();
        List<Long> expiredRoomIds = activeSharings.entrySet().stream()
                .filter(entry -> entry.getValue().isReconnectExpired(now) || entry.getValue().isStale(now))
                .map(Map.Entry::getKey)
                .toList();
        expiredRoomIds.forEach(roomId -> close(roomId, activeSharings.get(roomId), now));

        sharingLogRepository.abandonStaleSharings(
                SharingLogStatus.ABANDONED,
                SharingLogStatus.SHARING,
                toLocalDateTime(now.minus(STALE_THRESHOLD))
        );
    }

    private void close(long roomId, ActiveSharing active, Instant now) {
        activeSharings.remove(roomId, active);
        SharingLogEntity sharingLog = sharingLogRepository.findById(active.logId()).orElse(null);
        if (sharingLog == null) {
            return;
        }
        LocalDateTime endedAt = toLocalDateTime(active.disconnectedAt() == null ? now : active.disconnectedAt());
        if (active.isOnLastTimeBox()) {
            sharingLog.finish(endedAt);
            publishFinished(active, sharingLog);
            return;
        }
        sharingLog.abandon(endedAt);
    }

    private void publishFinished(ActiveSharing active, SharingLogEntity sharingLog) {
        eventPublisher.publishEvent(new SharingFinishedEvent(
                sharingLog.getId(),
                sharingLog.getMemberId(),
                active.memberEmail(),
                sharingLog.getTableId(),
                active.tableName(),
                sharingLog.getStartedAt(),
                sharingLog.getEndedAt(),
                sharingLog.getDurationSeconds(),
                sharingLog.getAudienceCount()
        ));
    }

    private int toDisplayedSequence(TimerEventType eventType, int sequence) {
        if (eventType == TimerEventType.NEXT) {
            return sequence + 1;
        }
        if (eventType == TimerEventType.BEFORE) {
            return Math.max(sequence - 1, 0);
        }
        return sequence;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }

    private record ActiveSharing(
            long logId,
            String memberEmail,
            String tableName,
            int timeBoxCount,
            Instant startedAt,
            @Nullable Integer lastSequence,
            @Nullable Instant disconnectedAt
    ) {

        // 타임박스 순서는 0부터 시작한다
        private boolean isOnLastTimeBox() {
            return lastSequence != null && lastSequence >= timeBoxCount - 1;
        }

        private boolean isReconnectExpired(Instant now) {
            return disconnectedAt != null && !now.isBefore(disconnectedAt.plus(RECONNECT_GRACE));
        }

        private boolean isStale(Instant now) {
            return !now.isBefore(startedAt.plus(STALE_THRESHOLD));
        }

        private ActiveSharing withSequence(int sequence) {
            return new ActiveSharing(logId, memberEmail, tableName, timeBoxCount, startedAt, sequence,
                    disconnectedAt);
        }

        private ActiveSharing disconnected(Instant at) {
            if (disconnectedAt != null) {
                return this;
            }
            return new ActiveSharing(logId, memberEmail, tableName, timeBoxCount, startedAt, lastSequence, at);
        }

        private ActiveSharing reconnected() {
            return new ActiveSharing(logId, memberEmail, tableName, timeBoxCount, startedAt, lastSequence, null);
        }
    }
}
