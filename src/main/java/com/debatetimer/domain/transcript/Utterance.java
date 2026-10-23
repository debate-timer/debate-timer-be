package com.debatetimer.domain.transcript;

/**
 * 음성 인식이 확정한 한 문장. offsetMs는 토론 기록 시작 시점부터의 경과 시간이다.
 */
public record Utterance(long offsetMs, String text) {
}
