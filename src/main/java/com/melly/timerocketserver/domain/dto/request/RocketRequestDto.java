package com.melly.timerocketserver.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RocketRequestDto {
    private String name;
    private String design;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockExpiredAt;
    private String receiverType;
    private String receiverEmail;
    private String content;
}
