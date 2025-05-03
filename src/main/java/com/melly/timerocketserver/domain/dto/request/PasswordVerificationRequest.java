package com.melly.timerocketserver.domain.dto.request;

import lombok.*;

@Getter
@Setter
public class PasswordVerificationRequest {
    private String email;
    private String tempPassword;
}
