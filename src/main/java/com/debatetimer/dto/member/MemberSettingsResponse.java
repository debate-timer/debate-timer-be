package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Language;
import com.debatetimer.domain.member.MemberSettings;
import java.math.BigDecimal;

public record MemberSettingsResponse(Language language, BigDecimal volume) {

    public MemberSettingsResponse(MemberSettings settings) {
        this(settings.getLanguage(), settings.getVolume());
    }
}
