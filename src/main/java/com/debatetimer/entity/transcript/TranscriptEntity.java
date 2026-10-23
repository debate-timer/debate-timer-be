package com.debatetimer.entity.transcript;

import com.debatetimer.domain.transcript.SttMode;
import com.debatetimer.domain.transcript.TranscriptStatus;
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
@Table(name = "transcript")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranscriptEntity extends BaseTimeEntity {

    public static final int ERROR_MESSAGE_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private long tableId;

    @Column(name = "member_id")
    private long memberId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TranscriptStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "stt_mode")
    private SttMode sttMode;

    @NotNull
    @Column(name = "lines_json", columnDefinition = "LONGTEXT")
    private String linesJson;

    @Column(name = "raw_lines_json", columnDefinition = "LONGTEXT")
    private String rawLinesJson;

    @Column(name = "refining")
    private boolean refining;

    @Column(name = "report_json", columnDefinition = "LONGTEXT")
    private String reportJson;

    @Column(name = "error_message", length = ERROR_MESSAGE_MAX_LENGTH)
    private String errorMessage;

    public TranscriptEntity(long tableId, long memberId, SttMode sttMode, String linesJson) {
        this.tableId = tableId;
        this.memberId = memberId;
        this.sttMode = sttMode;
        this.linesJson = linesJson;
        this.rawLinesJson = linesJson;
        this.refining = true;
        this.status = TranscriptStatus.PROCESSING;
    }

    public void refine(String refinedLinesJson) {
        this.linesJson = refinedLinesJson;
        this.refining = false;
    }

    // 다듬기에 실패하면 원문을 그대로 속기록으로 쓴다.
    public void skipRefinement() {
        this.refining = false;
    }

    public void complete(String reportJson) {
        this.reportJson = reportJson;
        this.errorMessage = null;
        this.status = TranscriptStatus.DONE;
    }

    public void fail(String errorMessage) {
        this.errorMessage = truncate(errorMessage);
        this.status = TranscriptStatus.FAILED;
    }

    private String truncate(String message) {
        if (message == null || message.length() <= ERROR_MESSAGE_MAX_LENGTH) {
            return message;
        }
        return message.substring(0, ERROR_MESSAGE_MAX_LENGTH);
    }
}
