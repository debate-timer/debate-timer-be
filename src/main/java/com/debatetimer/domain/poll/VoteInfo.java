package com.debatetimer.domain.poll;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class VoteInfo {

    private static final Comparator<Vote> VOTE_COMPARATOR = Comparator.comparing(Vote::getCreatedAt);
    private static final long INITIAL_VOTE_COUNT = 0L;

    private final long pollId;
    private final long totalCount;
    private final long prosCount;
    private final long consCount;
    private final List<ParticipantName> voterNames;

    public VoteInfo(long pollId, List<Vote> votes) {
        Map<VoteTeam, Long> voteCounts = createVoteCounts(votes);
        this.pollId = pollId;
        this.totalCount = votes.size();
        this.prosCount = voteCounts.getOrDefault(VoteTeam.PROS, INITIAL_VOTE_COUNT);
        this.consCount = voteCounts.getOrDefault(VoteTeam.CONS, INITIAL_VOTE_COUNT);
        this.voterNames = votes.stream()
                .sorted(VOTE_COMPARATOR)
                .map(Vote::getName)
                .toList();
    }

    private Map<VoteTeam, Long> createVoteCounts(List<Vote> votes) {
        return votes.stream()
                .collect(Collectors.groupingBy(Vote::getTeam, Collectors.counting()));
    }
}
