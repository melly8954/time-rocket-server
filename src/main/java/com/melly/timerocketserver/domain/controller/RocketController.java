package com.melly.timerocketserver.domain.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rockets")
public class RocketController {
    @GetMapping("")
    public String index() {
        return "로켓 페이지입니다.";
    }
}
