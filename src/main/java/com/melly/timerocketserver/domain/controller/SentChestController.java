package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.SentChestDetailResponse;
import com.melly.timerocketserver.domain.dto.response.SentChestPageResponse;
import com.melly.timerocketserver.domain.service.SentChestService;
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
@RequestMapping("/api/sent-chests")
public class SentChestController implements ResponseController {
    private final SentChestService sentChestService;

    public SentChestController(SentChestService sentChestService){
        this.sentChestService = sentChestService;
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto> getSentChestList(@RequestParam(name = "rocket-name", required = false, defaultValue = "") String rocketName,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "sentChestId") String sort,
                                                        @RequestParam(defaultValue = "desc") String order){
        // 음수 혹은 0 페이지 방지 (최소 1 페이지부터 시작, 음수를 넣어도 1부터 시작)
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        Sort sortBy = Sort.by(Sort.Order.by(sort));
        sortBy = order.equalsIgnoreCase("desc") ? sortBy.descending() : sortBy.ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sortBy);

        SentChestPageResponse sentChestList = sentChestService.getSentChestList(getUserId(), rocketName, pageable);
        return makeResponseEntity(HttpStatus.OK, "송신 보관함에 저장된 로켓 목록을 불러왔습니다.", sentChestList);
    }

    @GetMapping("/{sentChestId}")
    public ResponseEntity<ResponseDto> getSentChestDetail(@PathVariable @Min(value = 1, message = "sentChestId는 1 이상이어야 합니다.") Long sentChestId){
        SentChestDetailResponse sentChestDetail = sentChestService.getSentChestDetail(getUserId(), sentChestId);
        return makeResponseEntity(HttpStatus.OK, "송신 보관함의 로켓 상세 정보를 불러왔습니다.", sentChestDetail);
    }

    @PatchMapping("/{sentChestId}/deleted-flag")
    public ResponseEntity<ResponseDto> softDeleteSentChest(@PathVariable @Min(value = 1, message = "sentChestId는 1 이상이어야 합니다.") Long sentChestId){
        sentChestService.softDeleteSentChest(getUserId(), sentChestId);
        return makeResponseEntity(HttpStatus.OK, "해당 송신 내역을 삭제했습니다.", null);
    }


    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getUserId();
    }
}
