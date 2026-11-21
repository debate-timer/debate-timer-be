package com.debatetimer.service.member;

import com.debatetimer.domain.member.Language;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domainrepository.member.MemberFeatureFlagDomainRepository;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberFeatureFlagsResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.MemberSettingsResponse;
import com.debatetimer.dto.member.MemberSettingsUpdateRequest;
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
    private final MemberFeatureFlagDomainRepository memberFeatureFlagDomainRepository;

    @Transactional(readOnly = true)
    public TableResponses getTables(long memberId) {
        Member member = memberRepository.getById(memberId);
        return customizeTableRepository.findAllByMember(member)
                .stream()
                .map(CustomizeTableEntity::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), TableResponses::from));
    }

    public MemberFeatureFlagsResponse getFeatureFlags(long memberId) {
        return new MemberFeatureFlagsResponse(memberFeatureFlagDomainRepository.getFlags(memberId));
    }

    @Transactional(readOnly = true)
    public MemberSettingsResponse getSettings(long memberId) {
        Member member = memberRepository.getById(memberId);
        return new MemberSettingsResponse(member.getSettings());
    }

    @Transactional
    public MemberSettingsResponse updateSettings(long memberId, MemberSettingsUpdateRequest request) {
        Member member = memberRepository.getById(memberId);
        member.updateSettings(request.language(), request.volume());
        return new MemberSettingsResponse(member.getSettings());
    }

    // 언어는 신규 회원을 만들 때만 반영하고, 기존 회원의 설정은 덮어쓰지 않는다
    @Transactional
    public MemberCreateResponse createMember(MemberInfo memberInfo, Language language) {
        Member member = memberRepository.findByEmail(memberInfo.email())
                .orElseGet(() -> memberRepository.save(memberInfo.toMember(language)));
        return new MemberCreateResponse(member);
    }
}
