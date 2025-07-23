package com.debatetimer.repository.poll;

import com.debatetimer.entity.poll.PollEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollJpaRepository extends JpaRepository<PollEntity, Long> {

    Optional<PollEntity> findByIdAndMemberId(long id, long memberId);

}
