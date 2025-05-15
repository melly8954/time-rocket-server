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
    private String rocketName;
    private String designUrl;
    private String senderEmail;
    private String receiverNickname;
    private String content;
    private String location;
    
    // entity 값을 dto 에 설정
    public PublicChestDto(ChestEntity chestEntity) {
        this.chestId = chestEntity.getChestId();
        this.rocketName = chestEntity.getRocket().getRocketName();
        this.designUrl = chestEntity.getRocket().getDesign();
        this.senderEmail = chestEntity.getRocket().getSenderUser().getEmail();
        this.receiverNickname = chestEntity.getRocket().getReceiverUser().getNickname();
        this.content = chestEntity.getRocket().getContent();
        this.location = chestEntity.getLocation();
    }
}