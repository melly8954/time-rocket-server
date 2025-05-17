package com.melly.timerocketserver.domain.controller;

import com.melly.timerocketserver.domain.dto.response.PublicChestDto;
import com.melly.timerocketserver.domain.service.DisplayService;
import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/displays")
public class DisplayController implements ResponseController {
    private final DisplayService displayService;

    public DisplayController(DisplayService displayService) {
        this.displayService = displayService;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseDto> getDisplayList(@PathVariable Long userId) {
        List<PublicChestDto> displayList = displayService.getDisplayList(userId);
        return makeResponseEntity(HttpStatus.OK, "진열장 조회 성공", displayList);
    }
}
