package com.debatetimer.service.member;

import com.debatetimer.controller.member.dto.MemberCreateRequest;
import com.debatetimer.controller.member.dto.MemberCreateResponse;
import com.debatetimer.controller.member.dto.TableResponses;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary_debate.ParliamentaryTable;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary_debate.ParliamentaryTableRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ParliamentaryTableRepository parliamentaryTableRepository;

    @Transactional(readOnly = true)
    public TableResponses getTables(Long memberId) {
        Member member = memberRepository.getById(memberId);
        List<ParliamentaryTable> parliamentaryTable = parliamentaryTableRepository.findAllByMember(member);
        return TableResponses.from(parliamentaryTable);
    }

    @Transactional
    public MemberCreateResponse createMember(MemberCreateRequest request) {
        Member member = memberRepository.save(request.toMember());
        return new MemberCreateResponse(member);
    }
}
