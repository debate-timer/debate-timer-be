package com.debatetimer.service.sharing;

import com.debatetimer.controller.tool.jwt.JwtTokenProvider;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.sharing.TimerEvent;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.ChairmanTokenResponse;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventDataResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharingService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomizeTableDomainRepository customizeTableDomainRepository;

    public SharingResponse share(SharingRequest request) {
        TimerEvent timerEvent = request.toTimerEvent();
        return Optional.ofNullable(timerEvent.getTimerEventData())
                .map(eventData -> new SharingResponse(
                        request.eventType(),
                        new TimerEventDataResponse(eventData)
                ))
                .orElse(new SharingResponse(request.eventType(), null));
    }

    public ChairmanTokenResponse issueChairmanToken(long tableId, Member member) {
        CustomizeTable customizeTable = customizeTableDomainRepository.getByIdAndMember(tableId, member);
        long debateTime = customizeTableDomainRepository.getTotalTimeBoxTimes(customizeTable.getId());
        String chairmanToken = jwtTokenProvider.createChairmanToken(new MemberInfo(member), debateTime * 2);
        return new ChairmanTokenResponse(chairmanToken);
    }
}
