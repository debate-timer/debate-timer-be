package com.debatetimer.domain.member;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSettings {

    public static final double MIN_VOLUME = 0.0;
    public static final double MAX_VOLUME = 1.0;
    private static final BigDecimal DEFAULT_VOLUME = new BigDecimal("0.5");
    private static final int VOLUME_SCALE = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 10)
    private Language language;

    @Column(name = "volume", nullable = false, precision = 2, scale = 1)
    private BigDecimal volume;

    private MemberSettings(Language language, BigDecimal volume) {
        this.language = language;
        this.volume = volume;
    }

    public static MemberSettings defaults(Language language) {
        return new MemberSettings(language, DEFAULT_VOLUME);
    }

    // null 인 값은 기존 값을 유지한다
    public MemberSettings update(Language language, Double volume) {
        if (language == null && volume == null) {
            throw new DTClientErrorException(ClientErrorCode.EMPTY_MEMBER_SETTINGS);
        }
        Language updatedLanguage = language == null ? this.language : language;
        BigDecimal updatedVolume = volume == null ? this.volume : toVolume(volume);
        return new MemberSettings(updatedLanguage, updatedVolume);
    }

    private BigDecimal toVolume(double volume) {
        if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_MEMBER_VOLUME);
        }
        return BigDecimal.valueOf(volume).setScale(VOLUME_SCALE, RoundingMode.HALF_UP);
    }
}
