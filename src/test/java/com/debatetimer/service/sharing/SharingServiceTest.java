package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SharingServiceTest extends BaseServiceTest {

    @Autowired
    private SharingService sharingService;

    @Nested
    class IssueChairmanToken {

        @Test
        void 사회자_토큰을_할당한다() {
            Member member = memberGenerator.generate("email@email.com");
            CustomizeTableEntity tableEntity = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxEntityGenerator.generate(tableEntity, CustomizeBoxType.NORMAL, 2);

            assertThatCode(() -> sharingService.issueChairmanToken(tableEntity.getId(), member))
                    .doesNotThrowAnyException();
        }

        @Test
        void 회원_소유의_테이블이_아니면_에러가_발생한다() {
            Member member = memberGenerator.generate("email@email.com");

            assertThatThrownBy(() -> sharingService.issueChairmanToken(1L, member))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }
}
