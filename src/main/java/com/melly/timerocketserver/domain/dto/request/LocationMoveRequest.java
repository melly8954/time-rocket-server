package com.melly.timerocketserver.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationMoveRequest {
    private String receiverType; // 수신자 타입 (self/other 등)
    private String newLocation;  // 새로운 위치 (예: "1-1")

}
