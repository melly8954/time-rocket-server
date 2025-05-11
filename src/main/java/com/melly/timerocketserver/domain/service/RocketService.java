package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import com.melly.timerocketserver.domain.repository.GroupRepository;
import com.melly.timerocketserver.domain.repository.RocketRepository;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RocketService {
    private final RocketRepository rocketRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ChestRepository chestRepository;

    public RocketService(RocketRepository rocketRepository,
                         UserRepository userRepository,
                         GroupRepository groupRepository,
                         ChestRepository chestRepository) {
        this.rocketRepository = rocketRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.chestRepository = chestRepository;
    }

    @Transactional
    public void sendRocket(Long userId, RocketRequestDto rocketRequestDto){
        String rocketName = rocketRequestDto.getRocketName();
        String rocketDesign = rocketRequestDto.getDesign();
        LocalDateTime rocketLockExpiredAt = rocketRequestDto.getLockExpiredAt();
        String rocketReceiverType = rocketRequestDto.getReceiverType();
        String rocketReceiverEmail = rocketRequestDto.getReceiverEmail();
        String rocketContent = rocketRequestDto.getContent();

        // 수신자, 발신자, 그룹 정보 가져오기 (예시: 이메일로 유저 찾기)
        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("보내는 사용자를 찾을 수 없습니다."));
        UserEntity receiver = userRepository.findByEmail(rocketReceiverEmail)
                .orElseThrow(() -> new UserNotFoundException("해당 회원은 존재하지 않습니다."));

        // RocketEntity 생성
        RocketEntity rocket = RocketEntity.builder()
                .name(rocketName)
                .design(rocketDesign)
                .lockExpiredAt(rocketLockExpiredAt)
                .receiverType(rocketReceiverType)
                .senderUser(sender)
                .receiverUser(receiver)
                .group(null)
                .content(rocketContent)
                .isLock(true)
                .tempStatus(false)
                .sentAt(LocalDateTime.now())
                .build();
        rocketRepository.save(rocket);

        // ChestEntity 생성 및 저장
        ChestEntity chest = ChestEntity.builder()
                .rocket(rocket)
                .isPublic(false)
                .publicAt(null)
                .location("A")
                .isDeleted(false)
                .build();
        chestRepository.save(chest);
    }

}
