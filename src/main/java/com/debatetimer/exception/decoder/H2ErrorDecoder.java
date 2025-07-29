package com.debatetimer.exception.decoder;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public class H2ErrorDecoder implements RepositoryErrorDecoder {

    protected static final String UNIQUE_CONSTRAINT_VIOLATION_SQL_STATE = "23505";

    @Override
    public boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                String sqlState = cve.getSQLException().getSQLState();
                return UNIQUE_CONSTRAINT_VIOLATION_SQL_STATE.equals(sqlState);
            }
            cause = cause.getCause();
        }
        return false;
    }
}
