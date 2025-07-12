package com.debatetimer.entity.poll;

import com.debatetimer.domain.poll.PollStatus;
import com.debatetimer.entity.customize.BaseTimeEntity;
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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "poll")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PollEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long tableId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PollStatus status;

    @NotBlank
    private String prosTeamName;

    @NotBlank
    private String consTeamName;

    private String agenda;
}
