package com.melly.timerocketserver.global.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountDeletedException extends AuthenticationException {
    public AccountDeletedException(String message) {
        super(message);
    }
}
