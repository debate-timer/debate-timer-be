package com.debatetimer.domain.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParliamentaryTable {

    private static final String NAME_REGEX = "^[a-zA-Z가-힣0-9 ]+$";
    public static final int NAME_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public ParliamentaryTable(
            Member member,
            String name,
            String agenda,
            int duration,
            boolean warningBell,
            boolean finishBell
    ) {
        validate(name, duration);
        this.member = member;
        this.name = name;
        this.agenda = agenda;
        this.duration = duration;
        this.warningBell = warningBell;
        this.finishBell = finishBell;
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

    public void update(ParliamentaryTable renewTable) {
        this.name = renewTable.getName();
        this.agenda = renewTable.getAgenda();
        this.duration = renewTable.getDuration();
        this.warningBell = renewTable.isWarningBell();
        this.finishBell = renewTable.isFinishBell();
    }

    public boolean isOwner(long memberId) {
        return Objects.equals(this.member.getId(), memberId);
    }
}
