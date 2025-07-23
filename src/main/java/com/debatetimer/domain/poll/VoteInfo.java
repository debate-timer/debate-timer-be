package com.debatetimer.domain.poll;

import lombok.Getter;

@Getter
public class VoteInfo {

    private final long pollId;
    private final long totalCount;
    private final long prosCount;
    private final long consCount;

    public VoteInfo(long pollId, long prosCount, long consCount) {
        this.pollId = pollId;
        this.totalCount = prosCount + consCount;
        this.prosCount = prosCount;
        this.consCount = consCount;
    }
}
