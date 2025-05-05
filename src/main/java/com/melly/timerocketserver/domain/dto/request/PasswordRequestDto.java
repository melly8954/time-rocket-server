package com.melly.timerocketserver.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRequestDto {
    private String currentPassword;
    private String newPassword;
}
