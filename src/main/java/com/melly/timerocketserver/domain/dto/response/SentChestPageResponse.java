package com.melly.timerocketserver.domain.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SentChestPageResponse {
    private List<SentChestPageResponse.SentChestDto> sentChests;
    private int currentPage;
    private int pageSize;
    private Long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private String sortBy;
    private String sortDirection;
    private Long sentCount;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SentChestDto {
        private Long sentChestId;
        private Long rocketId;
        private String rocketName;
        private String designUrl;
        private String senderEmail;
        private String receiverEmail;
        private String content;
    }
}
