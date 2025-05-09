package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.service.RocketService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rockets")
public class RocketController implements ResponseController {
    private final RocketService rocketService;

    public RocketController(RocketService rocketService) {
        this.rocketService = rocketService;
    }
    @PostMapping("/users/{userId}")
    public ResponseEntity<ResponseDto> sendRocket(@PathVariable Long userId, @RequestBody RocketRequestDto rocketRequestDto) {
        this.rocketService.sendRocket(userId, rocketRequestDto);
        return makeResponseEntity(HttpStatus.CREATED, "로켓이 전송 되었습니다.", null);
    }


}
