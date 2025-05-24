package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.SentChestPageResponse;
import com.melly.timerocketserver.domain.service.SentChestService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import com.melly.timerocketserver.global.security.CustomUserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser().getUserId();
    }
}
