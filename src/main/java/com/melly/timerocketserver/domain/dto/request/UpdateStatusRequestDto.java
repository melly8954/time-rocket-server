package com.melly.timerocketserver.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequestDto {
    private String status; // ACTIVE, INACTIVE, DELETED
}
