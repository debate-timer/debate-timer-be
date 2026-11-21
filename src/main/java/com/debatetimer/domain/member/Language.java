package com.debatetimer.domain.member;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;
import lombok.RequiredArgsConstructor;

/**
 * 회원이 선택한 언어
 * - 도메인과 DB는 상수 이름(KO_KR / US_EN)으로 저장한다.
 * - 클라이언트와는 언어 코드(ko / en)로 주고받는다.
 */
@RequiredArgsConstructor
public enum Language {

    KO_KR("ko"),
    US_EN("en"),
    ;

    private static final String ENGLISH_PREFIX = "en";

    private final String code;

    @JsonValue
    public String getCode() {
        return code;
    }

    // 지원하지 않는 언어 코드는 요청 본문을 읽을 수 없는 것으로 보고 거절한다
    @JsonCreator
    public static Language fromCode(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 언어 코드입니다: " + code));
    }

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
