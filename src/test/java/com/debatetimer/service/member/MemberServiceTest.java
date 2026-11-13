package com.debatetimer.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Language;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.MemberSettingsResponse;
import com.debatetimer.dto.member.MemberSettingsUpdateRequest;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
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

            MemberCreateResponse actual = memberService.createMember(request, Language.KO_KR);

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

            MemberCreateResponse actual = memberService.createMember(request, Language.KO_KR);

            assertThat(actual.id()).isEqualTo(existedMember.getId());
        }

        @Test
        void 신규_회원은_요청한_언어로_설정을_만든다() {
            MemberInfo request = new MemberInfo("default@gmail.com");

            MemberCreateResponse actual = memberService.createMember(request, Language.US_EN);

            Member foundMember = memberRepository.getById(actual.id());
            assertAll(
                    () -> assertThat(foundMember.getSettings().getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(foundMember.getSettings().getVolume()).isEqualByComparingTo("0.5")
            );
        }

        @Test
        void 기존_회원의_언어_설정은_덮어쓰지_않는다() {
            memberGenerator.generate("default@gmail.com");
            MemberInfo request = new MemberInfo("default@gmail.com");

            MemberCreateResponse actual = memberService.createMember(request, Language.US_EN);

            Member foundMember = memberRepository.getById(actual.id());
            assertThat(foundMember.getSettings().getLanguage()).isEqualTo(Language.KO_KR);
        }
    }

    @Nested
    class GetSettings {

        @Test
        void 저장된_설정이_없는_회원은_기본값을_조회한다() {
            Member member = memberGenerator.generate("default@gmail.com");

            MemberSettingsResponse response = memberService.getSettings(member.getId());

            assertAll(
                    () -> assertThat(response.language()).isEqualTo(Language.KO_KR),
                    () -> assertThat(response.volume()).isEqualByComparingTo("0.5")
            );
        }
    }

    @Nested
    class UpdateSettings {

        @Test
        void 회원의_설정을_변경한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            MemberSettingsUpdateRequest request = new MemberSettingsUpdateRequest(Language.US_EN, 0.75);

            MemberSettingsResponse response = memberService.updateSettings(member.getId(), request);

            Member foundMember = memberRepository.getById(member.getId());
            assertAll(
                    () -> assertThat(response.language()).isEqualTo(Language.US_EN),
                    () -> assertThat(response.volume()).isEqualByComparingTo("0.8"),
                    () -> assertThat(foundMember.getSettings().getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(foundMember.getSettings().getVolume()).isEqualByComparingTo("0.8")
            );
        }

        @Test
        void 요청하지_않은_설정은_유지한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            memberService.updateSettings(member.getId(), new MemberSettingsUpdateRequest(Language.US_EN, null));

            MemberSettingsResponse response = memberService.updateSettings(member.getId(),
                    new MemberSettingsUpdateRequest(null, 0.2));

            assertAll(
                    () -> assertThat(response.language()).isEqualTo(Language.US_EN),
                    () -> assertThat(response.volume()).isEqualByComparingTo("0.2")
            );
        }

        @Test
        void 변경할_설정이_없으면_예외를_던진다() {
            Member member = memberGenerator.generate("default@gmail.com");
            MemberSettingsUpdateRequest request = new MemberSettingsUpdateRequest(null, null);

            assertThatThrownBy(() -> memberService.updateSettings(member.getId(), request))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EMPTY_MEMBER_SETTINGS.getMessage());
        }
    }

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            customizeTableEntityGenerator.generate(member);
            customizeTableEntityGenerator.generate(member);

            TableResponses response = memberService.getTables(member.getId());

            assertThat(response.tables()).hasSize(2);
        }

        @Test
        void 회원의_전체_토론_시간표는_정해진_순서대로_반환한다() throws InterruptedException {
            Member member = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            Thread.sleep(1);

            TableResponses response = memberService.getTables(member.getId());

            assertAll(
                    () -> assertThat(response.tables().get(0).id()).isEqualTo(table.getId())
            );
        }
    }
}
