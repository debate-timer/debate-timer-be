package com.debatetimer.exception.decoder;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public class MySqlErrorDecoder implements RepositoryErrorDecoder {

    protected static final String MYSQL_UNIQUE_VIOLATION = "23000";
    protected static final int MYSQL_DUP_ERROR_CODE = 1062;

    @Override
    public boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                SQLException sqlEx = cve.getSQLException();
                String sqlState = sqlEx.getSQLState();
                int errorCode = sqlEx.getErrorCode();
                return MYSQL_UNIQUE_VIOLATION.equals(sqlState)
                        && MYSQL_DUP_ERROR_CODE == errorCode;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
