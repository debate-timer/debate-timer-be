package com.debatetimer.scheduler;

import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.poll.PollEntity;
import com.debatetimer.repository.poll.PollRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PollCleanupScheduler {

    private static final int INTERVAL_HOURS = 12;
    private static final long INTERVAL_MILLIS = INTERVAL_HOURS * 60 * 60 * 1000L;
    static final int TIMEOUT_HOURS = 3;

    private final PollRepository pollRepository;

    @Scheduled(fixedRate = INTERVAL_MILLIS)
    @Transactional
    public void cleanupStalePolls() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(TIMEOUT_HOURS);
        pollRepository.findAllByStatusAndCreatedAtBefore(PollStatus.PROGRESS, threshold)
                .forEach(PollEntity::updateToDone);
    }
}
