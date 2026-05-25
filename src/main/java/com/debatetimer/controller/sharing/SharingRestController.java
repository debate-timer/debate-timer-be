package com.debatetimer.controller.sharing;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.sharing.response.ChairmanTokenResponse;
import com.debatetimer.service.customize.CustomizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SharingRestController {

    private final CustomizeService customizeService;
    private final AuthManager authManager;

    @GetMapping("/api/live/{tableId}/chairman-token")
    public ChairmanTokenResponse issueChairmanToken(
            @AuthMember Member member,
            @PathVariable("tableId") long tableId
    ) {
        long debateTime = customizeService.findDebateTime(tableId, member);
        String chairmanToken = authManager.issueChairmanToken(member, debateTime * 2);
        return new ChairmanTokenResponse(chairmanToken);
    }
}
