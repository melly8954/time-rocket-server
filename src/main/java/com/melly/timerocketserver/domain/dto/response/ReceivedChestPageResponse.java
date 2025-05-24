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
public class ReceivedChestPageResponse {
    // 여러 개의 ChestDto 객체를 포함
    private List<ReceivedChestDto> receivedChests;
    private int currentPage;
    private int pageSize;
    private Long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private String sortBy;
    private String sortDirection;
    private Long receivedCount;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceivedChestDto {
        private Long receivedChestId;
        private Long rocketId;
        private String rocketName;
        private String designUrl;
        private String senderEmail;
        private String receiverNickname;
        private String receiverEmail;
        private String content;
        private LocalDateTime lockExpiredAt;
        @JsonProperty("isPublic")  // JSON 직렬화 시 'isPublic'으로 나오게 강제
        private boolean isPublic;
        private LocalDateTime publicAt;
    }
}
