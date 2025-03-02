package com.debatetimer.service.member;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.repository.member.MemberRepository;
import com.debatetimer.repository.parliamentary.ParliamentaryTableRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ParliamentaryTableRepository parliamentaryTableRepository;

    @Transactional(readOnly = true)
    public TableResponses getTables(Long memberId) {
        Member member = memberRepository.getById(memberId);
        List<ParliamentaryTable> parliamentaryTable = parliamentaryTableRepository.findAllByMember(member);
        log.info("회원 테이블 정보 조회 성공 - 회원 이메일 : {}", member.getEmail());
        return TableResponses.from(parliamentaryTable);
    }

    @Transactional
    public MemberCreateResponse createMember(MemberInfo memberInfo) {
        Member member = memberRepository.findByEmail(memberInfo.email())
                .orElseGet(() -> {
                    log.info("신규 회원 가입 - 회원 이메일 : {}", memberInfo.email());
                    return memberRepository.save(memberInfo.toMember());
                });
        return new MemberCreateResponse(member);
    }
}
