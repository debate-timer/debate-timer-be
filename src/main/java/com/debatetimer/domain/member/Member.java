package com.debatetimer.domain.member;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final String NICKNAME_REGEX = "^[a-zA-Z가-힣 ]+$";
    public static final int NICKNAME_MAX_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nickname;

    @Column(unique = true)
    @NotNull
    private String email;

    public Member(long id, String nickname, String email) {
        validate(nickname);
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }

    public Member(String nickname, String email) {
        validate(nickname);
        this.nickname = nickname;
        this.email = email;
    }

    private void validate(String nickname) {
        if (nickname.isEmpty() || nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_MEMBER_NICKNAME_LENGTH);
        }
        if (!nickname.matches(NICKNAME_REGEX)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_MEMBER_NICKNAME_FORM);
        }
    }
}
