package com.debatetimer.domain;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DebateTable extends BaseTimeEntity {

    private static final String NAME_REGEX = "^[a-zA-Z가-힣0-9 ]+$";
    public static final int NAME_MAX_LENGTH = 20;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    private String name;

    @NotNull
    private String agenda;

    private int duration;

    private boolean warningBell;

    private boolean finishBell;

    @NotNull
    private LocalDateTime usedAt;

    protected DebateTable(Member member, String name, String agenda, int duration, boolean warningBell,
                          boolean finishBell) {
        validate(name, duration);

        this.member = member;
        this.name = name;
        this.agenda = agenda;
        this.duration = duration;
        this.warningBell = warningBell;
        this.finishBell = finishBell;
        this.usedAt = LocalDateTime.now();
    }

    public final boolean isOwner(long memberId) {
        return Objects.equals(this.member.getId(), memberId);
    }

    protected final void updateTable(DebateTable renewTable) {
        validate(renewTable.getName(), renewTable.getDuration());

        this.name = renewTable.getName();
        this.agenda = renewTable.getAgenda();
        this.duration = renewTable.getDuration();
        this.warningBell = renewTable.isWarningBell();
        this.finishBell = renewTable.isFinishBell();
        this.usedAt = LocalDateTime.now();
    }

    private void validate(String name, int duration) {
        if (name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_NAME_LENGTH);
        }
        if (!name.matches(NAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_NAME_FORM);
        }
        if (duration <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_TIME);
        }
    }

    public abstract long getId();

    public abstract TableType getType();
}
