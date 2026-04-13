package com.debatetimer.controller.sharing;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.sharing.response.ChairmanTokenResponse;
import com.debatetimer.service.sharing.SharingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SharingRestController {

    private final SharingService sharingService;

    @GetMapping("/api/share/{tableId}/chairman-token")
    public ChairmanTokenResponse issueChairmanToken(
            @AuthMember Member member,
            @PathVariable("tableId") long tableId
    ) {
        return sharingService.issueChairmanToken(tableId, member);
    }
}
