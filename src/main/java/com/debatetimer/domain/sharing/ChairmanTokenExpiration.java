package com.debatetimer.domain.sharing;

import java.time.Duration;

/**
 * 사회자 토큰 유효시간 정책
 * - 토론이 예상보다 길어져도 공유가 끊기지 않도록 전체 토론 시간의 2배를 준다.
 * - 토론 시간이 짧거나 계산할 수 없는 테이블도 충분히 진행할 수 있도록 최소 유효시간을 보장한다.
 */
public final class ChairmanTokenExpiration {

    static final Duration MIN_EXPIRATION = Duration.ofHours(3);
    private static final int DEBATE_TIME_MULTIPLIER = 2;

    private ChairmanTokenExpiration() {
    }

    public static long secondsFor(long debateTimeSeconds) {
        return Math.max(debateTimeSeconds * DEBATE_TIME_MULTIPLIER, MIN_EXPIRATION.toSeconds());
    }
}
