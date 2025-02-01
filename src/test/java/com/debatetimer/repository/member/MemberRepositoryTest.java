package com.debatetimer.repository.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.BaseRepositoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    class GetById {

        @Test
        void 아이디에_해당하는_멤버를_반환한다() {
            Member bito = memberGenerator.generate("default@gmail.com");

            assertThat(memberRepository.getById(bito.getId()).getId()).isEqualTo(bito.getId());
        }

        @Test
        void 아이디에_해당하는_멤버가_없으면_예외를_발생시킨다() {
            Member bito = memberGenerator.generate("default@gmail.com");

            assertThatThrownBy(() -> memberRepository.getById(bito.getId() + 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class GetByNickname {

        @Test
        void 닉네임에_해당하는_멤버를_반환한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            assertThat(memberRepository.getByEmail(bito.getEmail()).getId()).isEqualTo(bito.getId());
        }

        @Test
        void 닉네임에_해당하는_멤버가_없으면_예외를_발생시킨다() {
            memberGenerator.generate("default@gmail.com");

            assertThatThrownBy(() -> memberRepository.getByEmail("notbito@gmail.com"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
