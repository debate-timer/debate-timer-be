package com.debatetimer.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LanguageTest {

    @Nested
    class FromAcceptLanguage {

        @ParameterizedTest
        @ValueSource(strings = {"en", "en-US", "EN-GB,en;q=0.9", "en-US,ko;q=0.8"})
        void 첫_번째_언어가_영어면_US_EN_이다(String acceptLanguage) {
            assertThat(Language.fromAcceptLanguage(acceptLanguage)).isEqualTo(Language.US_EN);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ko", "ko-KR,en;q=0.9", "ja-JP", "*"})
        void 첫_번째_언어가_영어가_아니면_KO_KR_이다(String acceptLanguage) {
            assertThat(Language.fromAcceptLanguage(acceptLanguage)).isEqualTo(Language.KO_KR);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void 헤더가_비어_있으면_KO_KR_이다(String acceptLanguage) {
            assertThat(Language.fromAcceptLanguage(acceptLanguage)).isEqualTo(Language.KO_KR);
        }

        @Test
        void 앞뒤_공백은_무시한다() {
            assertThat(Language.fromAcceptLanguage("  en-US")).isEqualTo(Language.US_EN);
        }
    }
}
