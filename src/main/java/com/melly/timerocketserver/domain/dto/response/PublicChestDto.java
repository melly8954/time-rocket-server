package com.melly.timerocketserver.domain.dto.response;

import com.melly.timerocketserver.domain.entity.ChestEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicChestDto {
    private Long chestId;
    private Long rocketId;
    private String rocketName;
    private String designUrl;
    private String rocketReceiveType;
    private String senderEmail;
    private String receiverNickname;
    private String content;
    private String displayLocation;
    
    // entity 값을 dto 에 설정
    public PublicChestDto(ChestEntity chestEntity) {
        this.chestId = chestEntity.getChestId();
        this.rocketId = chestEntity.getRocket().getRocketId();
        this.rocketName = chestEntity.getRocket().getRocketName();
        this.designUrl = chestEntity.getRocket().getDesign();
        this.rocketReceiveType = chestEntity.getRocket().getReceiverType();
        this.senderEmail = chestEntity.getRocket().getSenderUser().getEmail();
        this.receiverNickname = chestEntity.getRocket().getReceiverUser().getNickname();
        this.content = chestEntity.getRocket().getContent();
        this.displayLocation = chestEntity.getDisplayLocation();
    }
}