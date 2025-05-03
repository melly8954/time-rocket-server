package com.melly.timerocketserver.global.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto {
    private int code;
    private String message;
    private Object data;
}
