package com.debatetimer.dto.member;

import com.debatetimer.domain.member.Language;

public record MemberSettingsUpdateRequest(Language language, Double volume) {
}
