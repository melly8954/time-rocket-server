package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequestDto {
    @NotBlank(message = "email 항목은 필수 입력 항목입니다.")
    @Size(min = 10, max = 255, message = "email은 10~255자 사이 입니다.")
    private String email;

    @NotBlank(message = "비밀번호 항목은 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이 입니다.")
    private String password;

    @NotBlank(message = "닉네임 항목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 100, message = "닉네임은 2~20자 사이 입니다.")
    private String nickname;
}
