package com.melly.timerocketserver.domain.dto.response;

import com.melly.timerocketserver.domain.entity.ReceivedChestEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayDto {
    private Long receivedChestId;
    private Long rocketId;
    private String rocketName;
    private String designUrl;
    private String rocketReceiveType;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private Long displayLocation;
    
    // entity 값을 dto 에 설정
    public DisplayDto(ReceivedChestEntity receivedChestEntity) {
        this.receivedChestId = receivedChestEntity.getReceivedChestId();
        this.rocketId = receivedChestEntity.getRocket().getRocketId();
        this.rocketName = receivedChestEntity.getRocket().getRocketName();
        this.designUrl = receivedChestEntity.getRocket().getDesign();
        this.rocketReceiveType = receivedChestEntity.getRocket().getReceiverType();
        this.senderEmail = receivedChestEntity.getRocket().getSenderUser().getEmail();
        this.receiverEmail = receivedChestEntity.getRocket().getReceiverUser().getNickname();
        this.content = receivedChestEntity.getRocket().getContent();
        this.displayLocation = receivedChestEntity.getDisplayLocation();
    }
}