package com.debatetimer.domain.poll;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VoteInfoTest {

    @Nested
    class getVoterNames {

        @Test
        void 생성_순으로_투표자_이름을_정렬한다() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMinutesAgo = now.minusMinutes(1);
            LocalDateTime twoMinutesAgo = now.minusMinutes(2);
            long pollId = 1L;
            Vote nowVote = new Vote(1L, pollId, VoteTeam.PROS, "콜리1", "code1", now);
            Vote oneMinutesAgoVote = new Vote(2L, pollId, VoteTeam.PROS, "콜리2", "code2", oneMinutesAgo);
            Vote twoMinutesAgoVote = new Vote(3L, pollId, VoteTeam.PROS, "콜리3", "code3", twoMinutesAgo);

            VoteInfo voteInfo = new VoteInfo(pollId, List.of(nowVote, oneMinutesAgoVote, twoMinutesAgoVote));

            assertThat(voteInfo.getVoterNames())
                    .containsExactly(
                            twoMinutesAgoVote.getName().getValue(),
                            oneMinutesAgoVote.getName().getValue(),
                            nowVote.getName().getValue()
                    );
        }
    }
}
