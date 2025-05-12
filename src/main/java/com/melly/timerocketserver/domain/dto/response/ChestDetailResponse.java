package com.melly.timerocketserver.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChestDetailResponse {
    private String rocketName;
    private String designUrl;
    private String senderEmail;
    private LocalDateTime sentAt;

    // 아래 두 필드는 상황에 따라 하나만 채워짐
    private String content;               // 잠금 해제 상태에서만 포함
    private LocalDateTime lockExpiredAt; // 잠금 상태에서만 포함

    @JsonProperty("isLocked")  // JSON 직렬화 시 'isLocked' 으로 나오게 강제
    private boolean isLocked;
}
