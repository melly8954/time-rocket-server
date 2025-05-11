package com.melly.timerocketserver.domain.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketResponse {
    private String rocketName;
    private String design;
    private LocalDateTime lockExpiredAt;
    private String receiverType;
    private String receiverEmail;
    private String content;
}
