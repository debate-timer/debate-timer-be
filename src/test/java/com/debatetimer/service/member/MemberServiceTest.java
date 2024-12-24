package com.debatetimer.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseServiceTest;
import com.debatetimer.controller.member.dto.MemberCreateRequest;
import com.debatetimer.controller.member.dto.MemberCreateResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberServiceTest extends BaseServiceTest {

    @Autowired
    private MemberService memberService;

    @Nested
    class CreateMember {

        @Test
        void 회원를_생성한다() {
            MemberCreateRequest request = new MemberCreateRequest("커찬");

            MemberCreateResponse actual = memberService.createMember(request);

            assertAll(
                    () -> assertThat(actual.nickname()).isEqualTo("커찬"),
                    () -> assertThat(memberRepository.count()).isEqualTo(1L)
            );
        }
    }
}
