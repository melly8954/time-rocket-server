package com.melly.timerocketserver.domain.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketUnLockRequest {
    private Boolean rocketLockStatus;
}
