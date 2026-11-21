package com.debatetimer.entity.recordingv2;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "recording_turn_v2")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordingTurnV2Entity extends BaseTimeEntity {

    public static final int ERROR_MESSAGE_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private long sessionId;

    @NotNull
    @Column(name = "turn_key")
    private String turnKey;

    @Column(name = "time_box_index")
    private int timeBoxIndex;

    @Enumerated(EnumType.STRING)
    private Stance team;

    @Column(name = "started_at_epoch_ms")
    private long startedAtEpochMs;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RecordingTurnStatusV2 status;

    @Column(name = "candidate_count")
    private int candidateCount;

    @Column(name = "selected_device_id")
    private String selectedDeviceId;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "utterances_json", columnDefinition = "LONGTEXT")
    private String utterancesJson;

    @Column(name = "error_message", length = ERROR_MESSAGE_MAX_LENGTH)
    private String errorMessage;

    public RecordingTurnV2Entity(long sessionId, String turnKey, int timeBoxIndex, Stance team,
                                 long startedAtEpochMs, int candidateCount) {
        this.sessionId = sessionId;
        this.turnKey = turnKey;
        this.timeBoxIndex = timeBoxIndex;
        this.team = team;
        this.startedAtEpochMs = startedAtEpochMs;
        this.candidateCount = candidateCount;
        this.status = RecordingTurnStatusV2.PROCESSING;
    }

    // 전사한 후보가 처음 고른 후보가 아닐 수 있어 시작 시각·팀도 함께 바꾼다.
    public void complete(String selectedDeviceId, long startedAtEpochMs, Stance team, String s3Key,
                         String utterancesJson) {
        this.status = RecordingTurnStatusV2.DONE;
        this.selectedDeviceId = selectedDeviceId;
        this.startedAtEpochMs = startedAtEpochMs;
        this.team = team;
        this.s3Key = s3Key;
        this.utterancesJson = utterancesJson;
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        this.status = RecordingTurnStatusV2.FAILED;
        this.errorMessage = RecordingSessionV2Entity.truncate(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
    }
}
