package com.melly.timerocketserver.global.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountInActiveException extends AuthenticationException {
    public AccountInActiveException(String message) {
        super(message);
    }
}
