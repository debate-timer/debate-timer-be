package com.debatetimer.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.service.BaseServiceTest;
import java.util.Optional;
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

            Optional<Member> foundMember = memberRepository.findById(actual.id());
            assertAll(
                    () -> assertThat(actual.nickname()).isEqualTo(request.nickname()),
                    () -> assertThat(foundMember).isPresent()
            );
        }
    }

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberRepository.save(new Member("커찬"));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 A", "주제", 1800));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 B", "주제", 1900));

            TableResponses response = memberService.getTables(member.getId());

            assertThat(response.tables()).hasSize(2);
        }
    }
}
