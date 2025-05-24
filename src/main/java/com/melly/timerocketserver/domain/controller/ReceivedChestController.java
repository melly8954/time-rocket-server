package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.ReceivedChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ReceivedChestPageResponse;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/received-chests")
public class ReceivedChestController implements ResponseController {
    private final com.melly.timerocketserver.domain.service.ReceivedChestService receivedChestService;
    public ReceivedChestController(com.melly.timerocketserver.domain.service.ReceivedChestService receivedChestService){
        this.receivedChestService = receivedChestService;
    }

    // 회원별 보관함 로켓 조회
    @GetMapping()
    public ResponseEntity<ResponseDto> getChestList(@RequestParam(required = false, defaultValue = "self") String receiverType,
                                                    @RequestParam(name="rocket-name", required = false, defaultValue = "") String rocketName,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "receivedChestId") String sort,
                                                    @RequestParam(defaultValue = "desc") String order){
        // 음수 혹은 0 페이지 방지 (최소 1 페이지부터 시작, 음수를 넣어도 1부터 시작)
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        Sort sortBy = Sort.by(Sort.Order.by(sort));
        sortBy = order.equalsIgnoreCase("desc") ? sortBy.descending() : sortBy.ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sortBy);

        ReceivedChestPageResponse chestList = receivedChestService.getReceivedChestList(getUserId(), rocketName, pageable, receiverType);

        return makeResponseEntity(HttpStatus.OK, "보관함에 저장된 로켓 목록을 불러왔습니다.", chestList);
    }

    // 보관함 로켓 상세 조회
    @GetMapping("/{receivedChestId}")
    public ResponseEntity<ResponseDto> getChestDetail(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long receivedChestId){
        ReceivedChestDetailResponse chestDetail = receivedChestService.getChestDetail(getUserId(), receivedChestId);
        return makeResponseEntity(HttpStatus.OK, "보관함의 로켓 상세 정보를 불러왔습니다.", chestDetail);
    }

    // 보관함 로켓 공개 여부 변경
    @PatchMapping("/{receivedChestId}/visibility")
    public ResponseEntity<ResponseDto> toggleVisibility(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long receivedChestId){
        receivedChestService.toggleVisibility(getUserId(), receivedChestId);
        return makeResponseEntity(HttpStatus.OK, "로켓의 공개 여부가 변경되었습니다.", null);
    }

    // 보관함 로켓 논리 삭제
    @PatchMapping("/{receivedChestId}/deleted-flag")
    public ResponseEntity<ResponseDto> softDeleteChest(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long receivedChestId){
        receivedChestService.softDeleteChest(getUserId(), receivedChestId);
        return makeResponseEntity(HttpStatus.OK, "해당 로켓이 삭제되었습니다.", null);
    }

    // 유저 ID 추출
    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getUserId();
    }
}
