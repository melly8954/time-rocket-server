package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.request.LocationMoveRequest;
import com.melly.timerocketserver.domain.dto.response.ChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.ChestPageResponse;
import com.melly.timerocketserver.domain.service.ChestService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseDto> getChestList(@PathVariable Long userId,
                                                    @RequestParam(required = false, defaultValue = "") String rocketName,
                                                    @RequestParam int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "chestId") String sort,
                                                    @RequestParam(defaultValue = "desc") String order){
        if (page <= 0) page = 1;
        if (size <= 0) size = 10;

        Sort sortBy = Sort.by(Sort.Order.by(sort));
        sortBy = order.equalsIgnoreCase("desc") ? sortBy.descending() : sortBy.ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sortBy);

        ChestPageResponse chestList = this.chestService.getChestList(userId, rocketName, pageable);

        return makeResponseEntity(HttpStatus.OK, "보관함에 저장된 로켓 목록을 불러왔습니다.", chestList);
    }

    @GetMapping("/users/{userId}/details/{chestId}")
    public ResponseEntity<ResponseDto> getChestDetail(@PathVariable Long userId,
                                                      @PathVariable @Min(value = 1, message = "chestId는 1 이상이어야 합니다.") Long chestId){
        ChestDetailResponse chestDetail = this.chestService.getChestDetail(userId, chestId);
        return makeResponseEntity(HttpStatus.OK, "보관함의 로켓 상세 정보를 불러왔습니다.", chestDetail);
    }

    // 보관함 배치 이동
    @PutMapping("/move-location")
    public ResponseEntity<String> moveLocation(@RequestBody LocationMoveRequest request) {
        this.chestService.moveRocketLocation(request.getRocketId(), request.getReceiverType(), request.getNewLocation());
        return ResponseEntity.ok("로켓의 배치이동이 완료되었습니다.");
    }
}
