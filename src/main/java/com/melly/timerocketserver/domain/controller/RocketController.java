package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.dto.response.RocketResponse;
import com.melly.timerocketserver.domain.service.RocketService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/rockets")
public class RocketController implements ResponseController {
    private final RocketService rocketService;

    public RocketController(RocketService rocketService) {
        this.rocketService = rocketService;
    }

    // 로켓 전송
    @PostMapping(value = "/users/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> sendRocket(@PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId,
                                                  @RequestPart("data") @Validated RocketRequestDto rocketRequestDto,
                                                  @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        this.rocketService.sendRocket(userId, rocketRequestDto, files);
        return makeResponseEntity(HttpStatus.CREATED, "로켓이 전송 되었습니다.", null);
    }   
    
    // 로켓 임시저장
    @PostMapping("/users/{userId}/temp")
    public ResponseEntity<ResponseDto> saveTempRocket(@PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId,
                                                  @RequestBody RocketRequestDto rocketRequestDto) {
        this.rocketService.saveTempRocket(userId, rocketRequestDto);
        return makeResponseEntity(HttpStatus.OK, "로켓이 임시저장 되었습니다.", null);
    }
    
    // 로켓 임시저장 불러오기
    @GetMapping("/users/{userId}/temp")
    public ResponseEntity<ResponseDto> getTempRocket(@PathVariable @Min(value = 1, message = "userId는 1 이상이어야 합니다.") Long userId) {
        RocketResponse tempRocket = this.rocketService.getTempRocket(userId);
        return makeResponseEntity(HttpStatus.OK, "임시저장 로켓을 불러왔습니다.", tempRocket);
    }

    // 로켓 잠금 해제
    @PatchMapping("/{rocketId}/unlocked-rocket")
    public ResponseEntity<ResponseDto> unlockRocket(@PathVariable @Min(value = 1, message = "rocketId는 1 이상이어야 합니다.") Long rocketId){
        this.rocketService.unlockRocket(rocketId);
        return makeResponseEntity(HttpStatus.OK, "로켓의 잠금이 해제되었습니다.", null);
    }
}
