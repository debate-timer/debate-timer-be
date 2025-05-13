package com.debatetimer.service.member;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponse;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import com.debatetimer.repository.member.MemberRepository;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Comparator<DebateTable> DEBATE_TABLE_COMPARATOR = Comparator
            .comparing(DebateTable::getUsedAt)
            .reversed();

    private final MemberRepository memberRepository;
    private final CustomizeTableRepository customizeTableRepository;

    @Transactional(readOnly = true)
    public TableResponses getTables(long memberId) {
        Member member = memberRepository.getById(memberId);
        return customizeTableRepository.findAllByMember(member).stream()
                .sorted(DEBATE_TABLE_COMPARATOR)
                .map(TableResponse::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(), TableResponses::new));
    }

    @Transactional
    public MemberCreateResponse createMember(MemberInfo memberInfo) {
        Member member = memberRepository.findByEmail(memberInfo.email())
                .orElseGet(() -> memberRepository.save(memberInfo.toMember()));
        return new MemberCreateResponse(member);
    }
}
