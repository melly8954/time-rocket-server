package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.DisplayLocationMoveRequest;
import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.DisplayDetailResponse;
import com.melly.timerocketserver.domain.dto.response.PublicChestDto;
import com.melly.timerocketserver.domain.service.DisplayService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/displays")
public class DisplayController implements ResponseController {
    private final DisplayService displayService;

    public DisplayController(DisplayService displayService) {
        this.displayService = displayService;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseDto> getDisplayList(@PathVariable Long userId) {
        List<PublicChestDto> displayList = displayService.getDisplayList(userId);
        return makeResponseEntity(HttpStatus.OK, "진열장 조회 성공", displayList);
    }

    @GetMapping("/users/{userId}/details/{chestId}")
    public ResponseEntity<ResponseDto> getDisplayDetail(@PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId,
                                                        @PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long chestId){
        DisplayDetailResponse displayDetail = displayService.getDisplayDetail(userId, chestId);
        return makeResponseEntity(HttpStatus.OK, "진열장의 로켓 상세 정보를 불러왔습니다.", displayDetail);
    }

    // 진열장 로켓 배치 이동
    @PatchMapping("/location")
    public ResponseEntity<ResponseDto> moveLocation(@RequestBody DisplayLocationMoveRequest request) {
        displayService.moveLocation(request.getSourceChestId(), request.getTargetChestId());
        return makeResponseEntity(HttpStatus.OK, "진열장의 로켓 배치이동이 완료되었습니다.", null);
    }
}
