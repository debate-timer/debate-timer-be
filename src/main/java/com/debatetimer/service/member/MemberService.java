package com.debatetimer.service.member;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import com.debatetimer.repository.timebased.TimeBasedTableRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ParliamentaryTableRepository parliamentaryTableRepository;
    private final TimeBasedTableRepository timeBasedTableRepository;

    @Transactional(readOnly = true)
    public TableResponses getTables(long memberId) {
        Member member = memberRepository.getById(memberId);
        List<ParliamentaryTable> parliamentaryTables = parliamentaryTableRepository.findAllByMember(member);
        List<TimeBasedTable> timeBasedTables = timeBasedTableRepository.findAllByMember(member);
        return new TableResponses(parliamentaryTables, timeBasedTables);
    }

    @Transactional
    public MemberCreateResponse createMember(MemberInfo memberInfo) {
        Member member = memberRepository.findByEmail(memberInfo.email())
                .orElseGet(() -> memberRepository.save(memberInfo.toMember()));
        return new MemberCreateResponse(member);
    }
}
