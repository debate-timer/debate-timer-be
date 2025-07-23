package com.debatetimer.domainrepository.poll;

import com.debatetimer.repository.poll.VoteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteDomainRepository {

    private final VoteJpaRepository voteJpaRepository;
}
