package com.melly.timerocketserver.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChestLocationMoveRequest {
    @NotNull
    private Long sourceChestId; // 이동할 로켓이 담긴 보관함 ID
    @NotNull
    private Long targetChestId; // 교환 대상 보관함 ID (빈 자리 이동 시에도 해당 자리 보관함 ID로)

}
