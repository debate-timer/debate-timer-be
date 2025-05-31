package com.debatetimer.fixture;

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
        CustomizeTableEntity table = new CustomizeTableEntity(
                member,
                "토론 테이블",
                "주제",
                "찬성",
                "반대",
                false,
                false,
                LocalDateTime.now()
        );
        return customizeTableRepository.save(table);
    }
}
