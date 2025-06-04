package com.debatetimer.fixture;

import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.repository.customize.CustomizeTableRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class CustomizeTableGenerator {

    private final CustomizeTableRepository customizeTableRepository;

    public CustomizeTableGenerator(CustomizeTableRepository customizeTableRepository) {
        this.customizeTableRepository = customizeTableRepository;
    }

    public CustomizeTableEntity generate(Member member) {
        CustomizeTable customizeTable = new CustomizeTable(
                member,
                "토론 테이블",
                "주제",
                "찬성",
                "반대",
                false,
                false,
                LocalDateTime.now()
        );
        CustomizeTableEntity table = new CustomizeTableEntity(customizeTable);
        return customizeTableRepository.save(table);
    }
}
