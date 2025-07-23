package com.debatetimer.entity.poll;

import com.debatetimer.domain.poll.Vote;
import com.debatetimer.domain.poll.VoteTeam;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "vote")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    private PollEntity poll;

    @NotNull
    @Enumerated(EnumType.STRING)
    private VoteTeam team;

    @NotBlank
    private String name;

    @NotBlank
    private String participantCode;

    public Vote toDomain() {
        return new Vote(id, poll.getId(), team, name, participantCode);
    }
}
