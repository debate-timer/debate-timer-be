package com.debatetimer.domainrepository.sharing;

import com.debatetimer.domain.sharing.SharingLog;
import com.debatetimer.domain.sharing.SharingLogStatus;
import com.debatetimer.entity.sharing.SharingLogEntity;
import com.debatetimer.repository.sharing.SharingLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class SharingLogDomainRepository {

    private final SharingLogRepository sharingLogRepository;

    @Transactional
    public SharingLog create(SharingLog sharingLog) {
        return sharingLogRepository.save(new SharingLogEntity(sharingLog))
                .toDomain();
    }

    @Transactional(readOnly = true)
    public SharingLog getById(long id) {
        return sharingLogRepository.getById(id)
                .toDomain();
    }

    @Transactional(readOnly = true)
    public List<SharingLog> findAllFinishedStartedBetween(LocalDateTime from, LocalDateTime to) {
        return sharingLogRepository.findAllByStatusAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                        SharingLogStatus.FINISHED, from, to)
                .stream()
                .map(SharingLogEntity::toDomain)
                .toList();
    }

    @Transactional
    public void increaseAudienceCount(long id) {
        sharingLogRepository.increaseAudienceCount(id);
    }

    /**
     * 진행 중인 기록을 종료한다. 다른 경로에서 이미 끝난 기록이면 바꾸지 않고 빈 값을 반환한다.
     */
    @Transactional
    public Optional<SharingLog> finish(long id, LocalDateTime endedAt) {
        int updated = sharingLogRepository.updateStatusIfSharing(
                id, SharingLogStatus.FINISHED, endedAt, SharingLogStatus.SHARING);
        if (updated == 0) {
            return Optional.empty();
        }
        return Optional.of(getById(id));
    }

    @Transactional
    public void abandon(long id, LocalDateTime endedAt) {
        sharingLogRepository.updateStatusIfSharing(id, SharingLogStatus.ABANDONED, endedAt, SharingLogStatus.SHARING);
    }

    @Transactional
    public void abandonStartedBefore(LocalDateTime threshold) {
        sharingLogRepository.abandonStaleSharings(SharingLogStatus.ABANDONED, SharingLogStatus.SHARING, threshold);
    }
}
