package com.melly.timerocketserver.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RocketRequestDto {
    private String rocketName;
    private String design;
    private LocalDateTime lockExpiredAt;
    private String receiverType;
    private String receiverEmail;
    private String content;
}
