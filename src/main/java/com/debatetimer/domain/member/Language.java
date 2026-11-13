package com.debatetimer.domain.member;

import java.util.Locale;

public enum Language {

    KO_KR,
    US_EN,
    ;

    private static final String ENGLISH_PREFIX = "en";

    // Accept-Language 의 첫 번째 언어가 영어면 US_EN, 그 외에는 KO_KR
    public static Language fromAcceptLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return KO_KR;
        }
        String primaryLanguage = acceptLanguage.strip().toLowerCase(Locale.ROOT);
        if (primaryLanguage.startsWith(ENGLISH_PREFIX)) {
            return US_EN;
        }
        return KO_KR;
    }
}
