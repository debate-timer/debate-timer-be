package com.debatetimer.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LanguageTest {

    @Nested
    class Code {

        @ParameterizedTest
        @CsvSource({"KO_KR, ko", "US_EN, en"})
        void 클라이언트와_주고받는_언어_코드를_가진다(Language language, String code) {
            assertThat(language.getCode()).isEqualTo(code);
        }

        @ParameterizedTest
        @CsvSource({"ko, KO_KR", "en, US_EN"})
        void 언어_코드로_언어를_찾는다(String code, Language language) {
            assertThat(Language.fromCode(code)).isEqualTo(language);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"KO_KR", "US_EN", "EN", "ko-KR", "ja"})
        void 지원하지_않는_언어_코드면_예외가_발생한다(String code) {
            assertThatThrownBy(() -> Language.fromCode(code))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

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
