package com.melly.timerocketserver.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SentChestDetailResponse {
    private Long rocketId;
    private String rocketName;
    private String designUrl;
    private String receiverEmail;
    private LocalDateTime sentAt;
    private String content;
    private LocalDateTime lockExpiredAt;
    private List<RocketFileResponse> rocketFiles;
}
