package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.dto.request.RocketUnLockRequest;
import com.melly.timerocketserver.domain.dto.response.RocketResponse;
import com.melly.timerocketserver.domain.service.RocketService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> sendRocket(@RequestPart("data") @Validated RocketRequestDto rocketRequestDto,
                                                  @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        rocketService.sendRocket(getUserId(), rocketRequestDto, files);
        return makeResponseEntity(HttpStatus.CREATED, "로켓이 전송 되었습니다.", null);
    }   
    
    // 로켓 임시저장
    @PostMapping("/temp-rockets")
    public ResponseEntity<ResponseDto> saveTempRocket(@RequestBody RocketRequestDto rocketRequestDto) {
        rocketService.saveTempRocket(getUserId(), rocketRequestDto);
        return makeResponseEntity(HttpStatus.OK, "로켓이 임시저장 되었습니다.", null);
    }
    
    // 로켓 임시저장 불러오기
    @GetMapping("/temp-rockets")
    public ResponseEntity<ResponseDto> getTempRocket() {
        RocketResponse tempRocket = rocketService.getTempRocket(getUserId());
        return makeResponseEntity(HttpStatus.OK, "임시저장 로켓을 불러왔습니다.", tempRocket);
    }

    // 로켓 잠금 해제
    @PatchMapping("/{rocketId}/unlock")
    public ResponseEntity<ResponseDto> unlockRocket(@PathVariable @Min(value = 1, message = "rocketId는 1 이상이어야 합니다.") Long rocketId){
        rocketService.unlockRocket(getUserId(), rocketId);
        return makeResponseEntity(HttpStatus.OK, "로켓의 잠금이 해제되었습니다.", null);
    }

    // 유저 ID 추출
    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getUserId();
    }
}
