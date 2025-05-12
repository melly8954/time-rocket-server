package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.dto.response.RocketResponse;
import com.melly.timerocketserver.domain.entity.ChestEntity;
import com.melly.timerocketserver.domain.entity.RocketEntity;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.ChestRepository;
import com.melly.timerocketserver.domain.repository.GroupRepository;
import com.melly.timerocketserver.domain.repository.RocketRepository;
import com.melly.timerocketserver.domain.repository.UserRepository;
import com.melly.timerocketserver.global.exception.RocketNotFoundException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
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
    
    // 로켓 전송
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
                .orElseThrow(() -> new UserNotFoundException("수신자 이메일을 찾을 수 없습니다."));

        // RocketEntity 생성
        RocketEntity rocket = RocketEntity.builder()
                .rocketName(rocketName)
                .design(rocketDesign)
                .lockExpiredAt(rocketLockExpiredAt)
                .receiverType(rocketReceiverType)
                .senderUser(sender)
                .receiverUser(receiver)
                .group(null)
                .content(rocketContent)
                .isLock(true)
                .isTemp(false)
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
    
    // 로켓 임시저장
    @Transactional
    public void tempRocket(Long userId, RocketRequestDto rocketRequestDto) {
        String rocketName = rocketRequestDto.getRocketName();
        String rocketDesign = rocketRequestDto.getDesign();
        LocalDateTime rocketLockExpiredAt = rocketRequestDto.getLockExpiredAt();
        String rocketReceiverType = rocketRequestDto.getReceiverType();
        String rocketReceiverEmail = rocketRequestDto.getReceiverEmail();
        String rocketContent = rocketRequestDto.getContent();

        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("보내는 사용자를 찾을 수 없습니다."));

        UserEntity receiver = userRepository.findByEmail(rocketReceiverEmail)
                .orElse(null);

        // 기존 임시 저장 로켓이 있는지 확인
        Optional<RocketEntity> existingTemp = rocketRepository
                .findBySenderUser_UserIdAndIsTemp(userId, true);

        RocketEntity tempRocket = existingTemp
                .orElseGet(() -> new RocketEntity());
        if (existingTemp.isPresent()) {
            log.info("기존 임시저장 로켓을 업데이트합니다.");
        } else {
            log.info("새로운 임시저장 로켓을 생성합니다.");
        }
        // 값 세팅 (새로 만들든, 기존 걸 업데이트하든)
        tempRocket.setRocketName(rocketName);
        tempRocket.setDesign(rocketDesign);
        tempRocket.setLockExpiredAt(rocketLockExpiredAt);
        tempRocket.setReceiverType(rocketReceiverType);
        tempRocket.setSenderUser(sender);
        tempRocket.setReceiverUser(receiver);
        tempRocket.setGroup(null);
        tempRocket.setContent(rocketContent);
        tempRocket.setIsLock(null);
        tempRocket.setIsTemp(true);
        tempRocket.setSentAt(null);
        tempRocket.setTempCreatedAt(LocalDateTime.now());
        rocketRepository.save(tempRocket); // insert or update
    }
    
    // 로켓 임시저장 불러오기
    public RocketResponse getTempRocket(Long userId) {
        if(userId == null || userId <= 0) {
            throw new UserNotFoundException("해당 회원은 존재하지 않습니다.");
        }
        Optional<RocketEntity> existingTemp = this.rocketRepository.findBySenderUser_UserIdAndIsTemp(userId, true);
        RocketEntity tempRocket = existingTemp
                .orElseThrow(() -> new RocketNotFoundException("해당 회원은 임시 저장된 로켓이 존재하지 않습니다."));

        String receiverEmail = tempRocket.getReceiverUser() != null
                ? tempRocket.getReceiverUser().getEmail()
                : null;
        // RocketEntity → RocketResponse 변환
        return RocketResponse.builder()
                .rocketName(tempRocket.getRocketName())
                .design(tempRocket.getDesign())
                .lockExpiredAt(tempRocket.getLockExpiredAt())
                .receiverType(tempRocket.getReceiverType())
                .receiverEmail(receiverEmail)
                .content(tempRocket.getContent())
                .build();
    }
}
