package com.melly.timerocketserver.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RocketFileResponse {
    private Long fileId;
    private String originalName;
    private String uniqueName;
    private String savedPath;  // 프론트가 이미지 URL로 사용할 필드
    private String fileType;
    private Long fileSize;
    private Integer fileOrder;
    private LocalDateTime uploadedAt;
}
