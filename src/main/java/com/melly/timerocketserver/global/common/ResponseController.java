package com.melly.timerocketserver.global.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface ResponseController {
    default ResponseEntity<ResponseDto> makeResponseEntity(HttpStatus httpStatus, String message, Object data) {
        ResponseDto responseDto = ResponseDto.builder()
                .code(httpStatus.value())
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(httpStatus).body(responseDto);
    }
}
