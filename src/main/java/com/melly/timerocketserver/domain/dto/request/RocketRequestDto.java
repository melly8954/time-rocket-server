package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RocketRequestDto {
    @NotBlank(message = "로켓 이름은 필수 항목입니다.")
    private String rocketName;

    @NotBlank(message = "로켓 디자인은 필수 항목입니다.")
    private String design;

    @NotNull(message = "잠금 해제일은 필수 항목입니다.")
    private LocalDateTime lockExpiredAt;

    @NotBlank(message = "수신자 유형은 필수 항목입니다.")
    private String receiverType;
    private String receiverEmail;

    @NotBlank(message = "로켓 내용은 필수 항목입니다.")
    private String content;
}
