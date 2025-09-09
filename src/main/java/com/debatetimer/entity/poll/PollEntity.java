package com.debatetimer.entity.poll;

import com.debatetimer.domain.poll.Poll;
import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "poll")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PollEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private long tableId;

    @Column(name = "member_id")
    private long memberId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PollStatus status;

    @NotBlank
    private String prosTeamName;

    @NotBlank
    private String consTeamName;

    private String agenda;

    public PollEntity(Poll poll) {
        this.id = poll.getId();
        this.tableId = poll.getTableId();
        this.memberId = poll.getMemberId();
        this.status = poll.getStatus();
        this.prosTeamName = poll.getProsTeamName().getValue();
        this.consTeamName = poll.getConsTeamName().getValue();
        this.agenda = poll.getAgenda().getValue();
    }

    public void updateToDone() {
        this.status = PollStatus.DONE;
    }

    public Poll toDomain() {
        return new Poll(id, tableId, memberId, status, prosTeamName, consTeamName, agenda);
    }
}
