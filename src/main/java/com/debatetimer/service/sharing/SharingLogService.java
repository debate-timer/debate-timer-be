package com.debatetimer.service.sharing;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.sharing.ActiveSharing;
import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.entity.sharing.SharingLogEntity;
import com.debatetimer.event.sharing.SharingFinishedEvent;
import com.debatetimer.repository.sharing.SharingLogRepository;
import java.time.Clock;
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
 * - 만료된 공유는 끝까지 진행했으면 종료로, 아니면 중단으로 기록한다. 만료 판단은 {@link ActiveSharing}이 한다.
 * - 종료로 기록할 때는 사회자가 직접 종료했는지와 관계없이 종료 이벤트를 발행한다.
 */
@Service
public class SharingLogService {

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
        LocalDateTime now = LocalDateTime.now(clock);
        ActiveSharing active = activeSharings.get(roomId);
        if (active != null && !active.isReconnectExpired(now)) {
            activeSharings.put(roomId, active.reconnect());
            return;
        }
        if (active != null) {
            close(roomId, active, now);
        }

        CustomizeTable table = customizeTableDomainRepository.getById(roomId);
        int timeBoxCount = customizeTableDomainRepository.countTimeBoxes(roomId);
        SharingLogEntity sharingLog = sharingLogRepository.save(
                new SharingLogEntity(roomId, table.getMember().getId(), now));
        activeSharings.put(roomId, new ActiveSharing(
                sharingLog.getId(),
                table.getMember().getEmail(),
                table.getName(),
                timeBoxCount,
                now
        ));
    }

    public void recordEvent(long roomId, TimerEvent timerEvent) {
        activeSharings.computeIfPresent(roomId, (id, active) -> active.record(timerEvent));
    }

    @Transactional
    public synchronized void finish(long roomId) {
        ActiveSharing active = activeSharings.remove(roomId);
        if (active == null) {
            return;
        }
        SharingLogEntity sharingLog = sharingLogRepository.findById(active.getLogId()).orElse(null);
        if (sharingLog == null) {
            return;
        }
        sharingLog.finish(LocalDateTime.now(clock));
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
        sharingLogRepository.increaseAudienceCount(active.getLogId());
    }

    public synchronized void disconnect(long roomId) {
        LocalDateTime now = LocalDateTime.now(clock);
        activeSharings.computeIfPresent(roomId, (id, active) -> active.disconnect(now));
    }

    /**
     * 재연결 유예 시간이 지난 공유와 너무 오래 이어진 공유를 정리한다.
     * 진행 중인 공유가 없으면 아무것도 하지 않는다.
     */
    @Transactional
    public synchronized void closeExpired() {
        if (activeSharings.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        List<Long> expiredRoomIds = activeSharings.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired(now))
                .map(Map.Entry::getKey)
                .toList();
        expiredRoomIds.forEach(roomId -> close(roomId, activeSharings.get(roomId), now));
    }

    /**
     * 서버 재시작 등으로 추적하지 못하게 된 기록을 중단으로 정리한다.
     * 오래된 기록만 대상으로 하므로 진행 중인 공유의 정리보다 드물게 실행해도 된다.
     */
    @Transactional
    public void abandonUntracked() {
        sharingLogRepository.abandonStaleSharings(
                SharingLogStatus.ABANDONED,
                SharingLogStatus.SHARING,
                LocalDateTime.now(clock).minus(ActiveSharing.STALE_THRESHOLD)
        );
    }

    private void close(long roomId, ActiveSharing active, LocalDateTime now) {
        activeSharings.remove(roomId, active);
        SharingLogEntity sharingLog = sharingLogRepository.findById(active.getLogId()).orElse(null);
        if (sharingLog == null) {
            return;
        }
        LocalDateTime endedAt = active.endedAt(now);
        if (active.isCompleted()) {
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
                active.getMemberEmail(),
                sharingLog.getTableId(),
                active.getTableName(),
                sharingLog.getStartedAt(),
                sharingLog.getEndedAt(),
                sharingLog.getDurationSeconds(),
                sharingLog.getAudienceCount()
        ));
    }
}
