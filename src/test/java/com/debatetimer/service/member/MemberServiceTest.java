package com.debatetimer.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.timebased.TimeBasedTable;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
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
            MemberInfo request = new MemberInfo("default@gmail.com");

            MemberCreateResponse actual = memberService.createMember(request);

            Optional<Member> foundMember = memberRepository.findById(actual.id());
            assertAll(
                    () -> assertThat(actual.email()).isEqualTo(request.email()),
                    () -> assertThat(foundMember).isPresent()
            );
        }

        @Test
        void 기존_닉네임을_가진_회원이_있다면_해당_회원을_반환한다() {
            Member existedMember = memberGenerator.generate("default@gmail.com");
            MemberInfo request = new MemberInfo("default@gmail.com");

            MemberCreateResponse actual = memberService.createMember(request);

            assertThat(actual.id()).isEqualTo(existedMember.getId());
        }
    }

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            parliamentaryTableGenerator.generate(member);
            timeBasedTableGenerator.generate(member);

            TableResponses response = memberService.getTables(member.getId());

            assertThat(response.tables()).hasSize(2);
        }

        @Test
        void 회원의_전체_토론_시간표는_정해진_순서대로_반환한다() throws InterruptedException {
            Member member = memberGenerator.generate("default@gmail.com");
            ParliamentaryTable table1 = parliamentaryTableGenerator.generate(member);
            TimeBasedTable table2 = timeBasedTableGenerator.generate(member);
            Thread.sleep(1);
            table1.updateUsedAt();

            TableResponses response = memberService.getTables(member.getId());

            assertAll(
                    () -> assertThat(response.tables().get(0).id()).isEqualTo(table1.getId()),
                    () -> assertThat(response.tables().get(1).id()).isEqualTo(table2.getId())
            );
        }
    }
}
