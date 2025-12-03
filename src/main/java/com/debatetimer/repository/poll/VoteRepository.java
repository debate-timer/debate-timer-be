package com.debatetimer.repository.poll;

import com.debatetimer.entity.poll.VoteEntity;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface VoteRepository extends Repository<VoteEntity, Long> {

    VoteEntity save(VoteEntity voteEntity);

    List<VoteEntity> findAllByPollId(long pollId);

    boolean existsByPollIdAndParticipateCode(long pollId, String participateCode);

    long count();
}
