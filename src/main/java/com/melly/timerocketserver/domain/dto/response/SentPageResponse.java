package com.melly.timerocketserver.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SentPageResponse {
    private List<SentDto> rockets;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private String sortBy;
    private String sortDirection;
    private long receivedCount;
    private long sentCount;

    @Getter @Builder
    public static class SentDto {
        private Long rocketSentId;    // RocketSentEntity의 PK
        private Long rocketId;
        private String rocketName;
        private String designUrl;
        private String senderEmail;
        private String receiverNickname;
        private String receiverEmail;
        private String content;
        private Boolean isLock;
        private LocalDateTime lockExpiredAt;
    }
}
