package com.debatetimer.service.sharing;

/**
 * 사회자 세션이 룸의 활성 사회자가 되려고 할 때의 결과
 */
public enum ChairmanClaim {

    /**
     * 활성 사회자가 없던 룸에서 새로 활성 사회자가 되었다.
     */
    NEW,

    /**
     * 연결된 다른 사회자를 밀어내고 활성 사회자가 되었다.
     */
    TAKEOVER,

    /**
     * 이미 활성 사회자이다. (재연결 포함)
     */
    SAME,

    /**
     * 다른 사회자에게 밀려난 세션이라 활성 사회자가 될 수 없다.
     */
    REJECTED,
    ;

    public boolean isAccepted() {
        return this != REJECTED;
    }

    public boolean isNewChairman() {
        return this == NEW || this == TAKEOVER;
    }
}
