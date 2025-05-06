package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {
    @NotBlank(message = "email 항목은 필수 입력 항목입니다.")
    @Size(min = 10, max = 255, message = "email은 10~255자 사이 입니다.")
    private String email;
}
