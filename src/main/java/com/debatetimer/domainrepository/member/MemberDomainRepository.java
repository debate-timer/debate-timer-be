package com.debatetimer.domainrepository.member;

import com.debatetimer.repository.member.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MemberDomainRepository {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<LocalDateTime> findCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return memberRepository.findCreatedAtBetween(from, to);
    }

    @Transactional(readOnly = true)
    public long countCreatedBefore(LocalDateTime createdAt) {
        return memberRepository.countByCreatedAtBefore(createdAt);
    }
}
