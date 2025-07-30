package com.debatetimer.exception.decoder;

import org.springframework.dao.DataIntegrityViolationException;

public interface RepositoryErrorDecoder {

    boolean isUniqueConstraintViolation(DataIntegrityViolationException exception);
}
