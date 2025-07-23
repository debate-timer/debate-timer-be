package com.debatetimer.repository.poll;

import com.debatetimer.entity.poll.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteEntity, Long> {

}
