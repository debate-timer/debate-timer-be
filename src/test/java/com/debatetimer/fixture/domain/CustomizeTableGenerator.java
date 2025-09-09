package com.debatetimer.fixture.domain;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class CustomizeTableGenerator {

    public CustomizeTable generate(Member member) {
        return new CustomizeTable(
                member,
                "토론 테이블",
                "주제",
                "찬성",
                "반대",
                false,
                false,
                LocalDateTime.now()
        );
    }

    public CustomizeTable generate(Member member, String name) {
        return new CustomizeTable(
                member,
                name,
                "주제",
                "찬성",
                "반대",
                false,
                false,
                LocalDateTime.now()
        );
    }
}
