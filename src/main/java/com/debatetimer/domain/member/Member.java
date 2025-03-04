package com.debatetimer.domain.member;

import com.debatetimer.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true)
    @NotNull
    private String email;

    public Member(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Member(String email) {
        this.email = email;
    }

    public boolean isSameMember(String email) {
        return this.email.equals(email);
    }
}
