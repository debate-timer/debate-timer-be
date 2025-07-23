package com.debatetimer.domainrepository.poll;

import com.debatetimer.repository.poll.PollJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PollDomainRepository {

    private final PollJpaRepository pollJpaRepository;
}
