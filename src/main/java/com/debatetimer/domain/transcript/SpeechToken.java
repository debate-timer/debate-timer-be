package com.debatetimer.domain.transcript;

/**
 * 배치 음성 인식이 돌려준 하위 단어 단위 토큰. 시각은 전사한 오디오 시작 시점 기준이다.
 */
public record SpeechToken(String text, long startMs, long endMs) {
}
