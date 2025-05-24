package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ReceivedChestPageResponse;
import com.melly.timerocketserver.domain.service.ChestService;
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
@RequestMapping("/api/chests")
public class ChestController implements ResponseController {
    private final ChestService chestService;
    public ChestController(ChestService chestService){
        this.chestService = chestService;
    }

    // 회원별 보관함 로켓 조회
    @GetMapping()
    public ResponseEntity<ResponseDto> getChestList(@RequestParam(required = false, defaultValue = "self") String receiverType,
                                                    @RequestParam(name="rocket-name", required = false, defaultValue = "") String rocketName,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "chestId") String sort,
                                                    @RequestParam(defaultValue = "desc") String order){
        // 음수 혹은 0 페이지 방지 (최소 1 페이지부터 시작, 음수를 넣어도 1부터 시작)
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        Sort sortBy = Sort.by(Sort.Order.by(sort));
        sortBy = order.equalsIgnoreCase("desc") ? sortBy.descending() : sortBy.ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sortBy);

        ReceivedChestPageResponse chestList = chestService.getReceivedChestList(getUserId(), rocketName, pageable, receiverType);

        return makeResponseEntity(HttpStatus.OK, "보관함에 저장된 로켓 목록을 불러왔습니다.", chestList);
    }

    // 보관함 로켓 상세 조회
    @GetMapping("/{chestId}")
    public ResponseEntity<ResponseDto> getChestDetail(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long chestId){
        ChestDetailResponse chestDetail = chestService.getChestDetail(getUserId(), chestId);
        return makeResponseEntity(HttpStatus.OK, "보관함의 로켓 상세 정보를 불러왔습니다.", chestDetail);
    }

    // 보관함 로켓 공개 여부 변경
    @PatchMapping("/{chestId}/visibility")
    public ResponseEntity<ResponseDto> toggleVisibility(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long chestId){
        chestService.toggleVisibility(getUserId(), chestId);
        return makeResponseEntity(HttpStatus.OK, "로켓의 공개 여부가 변경되었습니다.", null);
    }

    // 보관함 로켓 논리 삭제
    @PatchMapping("/{chestId}/deleted-flag")
    public ResponseEntity<ResponseDto> softDeleteChest(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long chestId){
        chestService.softDeleteChest(getUserId(), chestId);
        return makeResponseEntity(HttpStatus.OK, "해당 로켓이 삭제되었습니다.", null);
    }

    // 보관함 로켓 복구
    @PatchMapping("/{chestId}/restoration")
    public ResponseEntity<ResponseDto> restoreDeletedChest(@PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long chestId){
        chestService.restoreDeletedChest(getUserId(), chestId);
        return makeResponseEntity(HttpStatus.OK,"삭제된 로켓의 복구를 성공했습니다.",null);
    }

    // 유저 ID 추출
    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getUserId();
    }
}
