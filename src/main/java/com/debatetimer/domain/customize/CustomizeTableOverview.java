package com.debatetimer.domain.customize;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 어드민에서 테이블을 생성 순으로 살펴보기 위해 테이블과 생성 시각을 함께 담는다.
 */
@Getter
@RequiredArgsConstructor
public class CustomizeTableOverview {

    private final CustomizeTable table;
    private final LocalDateTime createdAt;
}
