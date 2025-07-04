package com.debatetimer.service.member;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.member.MemberRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CustomizeTableRepository customizeTableRepository;

    @Transactional(readOnly = true)
    public TableResponses getTables(long memberId) {
        Member member = memberRepository.getById(memberId);
        return customizeTableRepository.findAllByMember(member)
                .stream()
                .map(CustomizeTableEntity::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), TableResponses::from));
    }

    @Transactional
    public MemberCreateResponse createMember(MemberInfo memberInfo) {
        Member member = memberRepository.findByEmail(memberInfo.email())
                .orElseGet(() -> memberRepository.save(memberInfo.toMember()));
        return new MemberCreateResponse(member);
    }
}
