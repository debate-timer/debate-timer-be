package com.debatetimer.exception.decoder;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class H2ErrorDecoderTest {

    private H2ErrorDecoder errorDecoder;


    @BeforeEach
    void setUp() {
        errorDecoder = new H2ErrorDecoder();
    }

    @Nested
    class isUniqueError {

        @Test
        void 유니크_제약조건_에러를_판단할_수_있다() {
            SQLException uniqueError = new SQLException("유니크 에러", H2ErrorDecoder.UNIQUE_CONSTRAINT_VIOLATION_SQL_STATE);
            ConstraintViolationException uniqueViolation = new ConstraintViolationException("유니크 에러", uniqueError,
                    "vote_poll_id_participate_code");
            DataIntegrityViolationException uniqueException = new DataIntegrityViolationException("유니크 에러",
                    uniqueViolation);

            boolean isUniqueError = errorDecoder.isUniqueConstraintViolation(uniqueException);

            assertThat(isUniqueError).isTrue();
        }

        @Test
        void 유니크_제약조건_에러가_아님을_판단한다() {
            SQLException notUniqueError = new SQLException("다른 에러", "23000");
            ConstraintViolationException notUniqueViolation = new ConstraintViolationException("기타 에러", notUniqueError,
                    "some_constraint");
            DataIntegrityViolationException extraException = new DataIntegrityViolationException("에러",
                    notUniqueViolation);

            boolean isNotUniqueError = errorDecoder.isUniqueConstraintViolation(extraException);

            assertThat(isNotUniqueError).isFalse();
        }
    }
}
