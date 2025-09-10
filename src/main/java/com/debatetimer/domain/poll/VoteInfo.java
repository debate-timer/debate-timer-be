package com.debatetimer.domain.poll;

import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class VoteInfo {

    private static final long INITIAL_VOTE_COUNT = 0L;

    private final long pollId;
    private final long totalCount;
    private final long prosCount;
    private final long consCount;
    private final List<String> voterNames;

    public VoteInfo(long pollId, Map<VoteTeam, Long> voteCounts, List<String> voterNames) {
        this.pollId = pollId;
        this.prosCount = voteCounts.getOrDefault(VoteTeam.PROS, INITIAL_VOTE_COUNT);
        this.consCount = voteCounts.getOrDefault(VoteTeam.CONS, INITIAL_VOTE_COUNT);
        this.totalCount = prosCount + consCount;
        this.voterNames = voterNames;
    }
}
