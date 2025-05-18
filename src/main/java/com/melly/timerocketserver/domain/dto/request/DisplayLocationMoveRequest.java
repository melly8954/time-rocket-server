package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisplayLocationMoveRequest {
    @NotNull
    private Long sourceChestId; // 이동할 로켓이 담긴 보관함 ID
    @NotNull
    private Long targetChestId; // 교환 대상 보관함 ID

}
