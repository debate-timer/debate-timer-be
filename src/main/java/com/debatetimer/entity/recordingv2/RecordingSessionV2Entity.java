package com.debatetimer.entity.recordingv2;

import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "recording_session_v2")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordingSessionV2Entity extends BaseTimeEntity {

    public static final int ERROR_MESSAGE_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "session_key")
    private String sessionKey;

    @Column(name = "member_id")
    private long memberId;

    @Column(name = "table_id")
    private long tableId;

    @Column(name = "language")
    private String language;

    @NotNull
    @Column(name = "join_code_hash")
    private String joinCodeHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RecordingSessionStatusV2 status;

    @Column(name = "transcript_id")
    private Long transcriptId;

    @Column(name = "error_message", length = ERROR_MESSAGE_MAX_LENGTH)
    private String errorMessage;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public RecordingSessionV2Entity(String sessionKey, long memberId, long tableId, String language,
                                    String joinCodeHash) {
        this.sessionKey = sessionKey;
        this.memberId = memberId;
        this.tableId = tableId;
        this.language = language;
        this.joinCodeHash = joinCodeHash;
        this.status = RecordingSessionStatusV2.RECORDING;
    }

    public void finish(LocalDateTime finishedAt) {
        this.status = RecordingSessionStatusV2.FINISHING;
        this.finishedAt = finishedAt;
    }

    public void complete(long transcriptId) {
        this.status = RecordingSessionStatusV2.COMPLETED;
        this.transcriptId = transcriptId;
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        this.status = RecordingSessionStatusV2.FAILED;
        this.errorMessage = truncate(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
    }

    static String truncate(String message, int maxLength) {
        if (message == null || message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength);
    }
}
