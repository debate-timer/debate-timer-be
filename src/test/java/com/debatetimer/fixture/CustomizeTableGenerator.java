package com.debatetimer.fixture;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomizeTableGenerator {

    private final CustomizeTableRepository customizeTableRepository;

    public CustomizeTableGenerator(CustomizeTableRepository customizeTableRepository) {
        this.customizeTableRepository = customizeTableRepository;
    }

    public CustomizeTable generate(Member member) {
        CustomizeTable table = new CustomizeTable(
                member,
                "토론 테이블",
                "주제",
                false,
                false,
                "찬성",
                "반대"
        );
        return customizeTableRepository.save(table);
    }
}
