package com.debatetimer.repository.poll;

import com.debatetimer.entity.poll.VoteEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {

    List<VoteEntity> findAllByPollId(long pollId);
}
