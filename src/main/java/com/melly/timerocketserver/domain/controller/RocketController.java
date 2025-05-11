package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.dto.response.RocketResponse;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.service.RocketService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rockets")
public class RocketController implements ResponseController {
    private final RocketService rocketService;

    public RocketController(RocketService rocketService) {
        this.rocketService = rocketService;
    }
    // 로켓 전송
    @PostMapping("/users/{userId}")
    public ResponseEntity<ResponseDto> sendRocket(@PathVariable Long userId, @RequestBody @Validated RocketRequestDto rocketRequestDto) {
        this.rocketService.sendRocket(userId, rocketRequestDto);
        return makeResponseEntity(HttpStatus.CREATED, "로켓이 전송 되었습니다.", null);
    }   
    
    // 로켓 임시저장
    @PostMapping("/users/{userId}/temp")
    public ResponseEntity<ResponseDto> tempRocket(@PathVariable Long userId, @RequestBody RocketRequestDto rocketRequestDto) {
        this.rocketService.tempRocket(userId, rocketRequestDto);
        return makeResponseEntity(HttpStatus.OK, "로켓이 임시저장 되었습니다.", null);
    }
    
    // 로켓 임시저장 불러오기
    @GetMapping("/users/{userId}/temp")
    public ResponseEntity<ResponseDto> getTempRocket(@PathVariable Long userId) {
        RocketResponse tempRocket = this.rocketService.getTempRocket(userId);
        return makeResponseEntity(HttpStatus.OK, "임시저장 로켓을 불러왔습니다.", tempRocket);
    }
}
