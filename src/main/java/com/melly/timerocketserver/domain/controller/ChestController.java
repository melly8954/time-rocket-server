package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.ChestPageResponse;
import com.melly.timerocketserver.domain.service.ChestService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
