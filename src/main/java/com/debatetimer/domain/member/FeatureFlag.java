package com.debatetimer.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원별로 열어 주는 기능. DB 에는 이름(문자열)으로 저장하므로 값을 지우거나 이름을 바꿀 때는 저장된 행도 함께 옮긴다.
 */
@Getter
@RequiredArgsConstructor
public enum FeatureFlag {

    TRANSCRIPT("속기록 · 토론 분석"),
    MULTI_DEVICE_RECORDING("다기기 녹음 (V2)"),
    ;

    private final String description;
}
