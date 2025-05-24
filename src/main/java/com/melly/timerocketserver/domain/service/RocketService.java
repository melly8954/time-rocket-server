package com.melly.timerocketserver.domain.service;

import com.melly.timerocketserver.domain.dto.request.RocketRequestDto;
import com.melly.timerocketserver.domain.dto.response.RocketResponse;
import com.melly.timerocketserver.domain.entity.*;
import com.melly.timerocketserver.domain.repository.*;
import com.melly.timerocketserver.global.exception.RocketNotFoundException;
import com.melly.timerocketserver.global.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RocketService {
    private final RocketRepository rocketRepository;
    private final RocketFileRepository rocketFileRepository;
    private final UserRepository userRepository;
    private final ChestRepository chestRepository;
    private final SentChestRepository sentChestRepository;
    private final FileService fileService;

    public RocketService(RocketRepository rocketRepository, RocketFileRepository rocketFileRepository,
                         UserRepository userRepository, ChestRepository chestRepository,
                         SentChestRepository sentChestRepository, FileService fileService) {
        this.rocketRepository = rocketRepository;
        this.rocketFileRepository = rocketFileRepository;
        this.userRepository = userRepository;
        this.chestRepository = chestRepository;
        this.sentChestRepository = sentChestRepository;
        this.fileService = fileService;
    }

    // 로켓 전송
    @Transactional
    public void sendRocket(Long userId, RocketRequestDto rocketRequestDto, List<MultipartFile> files) throws IOException {
        String rocketName = rocketRequestDto.getRocketName();
        String rocketDesign = rocketRequestDto.getDesign();
        LocalDateTime rocketLockExpiredAt = rocketRequestDto.getLockExpiredAt();
        String rocketReceiverType = rocketRequestDto.getReceiverType();
        String rocketReceiverEmail = rocketRequestDto.getReceiverEmail();
        String rocketContent = rocketRequestDto.getContent();

        // 수신자, 발신자, 그룹 정보 가져오기 (예시: 이메일로 유저 찾기)
        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("보내는 사용자를 찾을 수 없습니다."));
        UserEntity receiver = userRepository.findByEmail(rocketReceiverEmail)
                .orElseThrow(() -> new UserNotFoundException("수신자 이메일을 찾을 수 없습니다."));

        // 나에게 보내는 로켓인데, 송신자 != 수신자인 경우
        if ("self".equalsIgnoreCase(rocketReceiverType) && !sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("자기 자신에게 보내는 로켓에서 수신자와 송신자가 달라서는 안 됩니다.");
        }

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

        // 파일 여러 개 저장
        if (files != null && !files.isEmpty()) {
            int order = 1;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // saveRocketFile 에서 저장과 고유명 생성 모두 처리
                    String savedPath = fileService.saveRocketFile(file);

                    // savedPath 의 가장 마지막 / 이후의 문자열을 추출
                    String uniqueName = savedPath.substring(savedPath.lastIndexOf("/") + 1);

                    RocketFileEntity rocketFile = RocketFileEntity.builder()
                            .rocket(rocket)
                            .originalName(file.getOriginalFilename())
                            .uniqueName(uniqueName)
                            .savedPath(savedPath)
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .fileOrder(order++)
                            .build();
                    rocketFileRepository.save(rocketFile);
                }
            }
        }

        // ChestEntity 생성 및 저장
        ChestEntity chest = ChestEntity.builder()
                .rocket(rocket)
                .isPublic(false)
                .publicAt(null)
                .isDeleted(false)
                .build();
        chestRepository.save(chest);

        // 보낸 로켓 관리 엔티티 저장
        SentChestEntity rocketSent = SentChestEntity.builder()
                .rocket(rocket)
                .isDeleted(false)
                .build();
        sentChestRepository.save(rocketSent);
    }

    // 로켓 임시저장
    @Transactional
    public void saveTempRocket(Long userId, RocketRequestDto rocketRequestDto) {
        String rocketName = rocketRequestDto.getRocketName();
        String rocketDesign = rocketRequestDto.getDesign();
        LocalDateTime rocketLockExpiredAt = rocketRequestDto.getLockExpiredAt();
        String rocketReceiverType = rocketRequestDto.getReceiverType();
        String rocketReceiverEmail = rocketRequestDto.getReceiverEmail();
        String rocketContent = rocketRequestDto.getContent();

        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("송신 회원을 찾을 수 없습니다."));

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
        RocketEntity findEntity = rocketRepository.findBySenderUser_UserIdAndIsTemp(userId, true)
                .orElseThrow(() -> new RocketNotFoundException("해당 회원은 임시 저장된 로켓이 존재하지 않습니다."));

        String receiverEmail = findEntity.getReceiverUser() != null
                ? findEntity.getReceiverUser().getEmail()
                : null;

        // RocketEntity → RocketResponse 변환
        return RocketResponse.builder()
                .rocketName(findEntity.getRocketName())
                .design(findEntity.getDesign())
                .lockExpiredAt(findEntity.getLockExpiredAt())
                .receiverType(findEntity.getReceiverType())
                .receiverEmail(receiverEmail)
                .content(findEntity.getContent())
                .build();
    }

    public void unlockRocket(Long userId, Long rocketId) {
        RocketEntity findEntity = rocketRepository.findByRocketIdAndIsLockTrue(rocketId)
                .orElseThrow(()-> new RocketNotFoundException("해당 로켓은 존재하지 않거나 이미 잠금이 해제된 로켓입니다."));

        if (!findEntity.getReceiverUser().getUserId().equals(userId)) {
            throw new IllegalStateException("해당 로켓에 대한 권한이 없습니다.");
        }

        // 잠금 해제 가능 조건: lockExpiredAt가 현재 시각 이전 또는 같음
        if (findEntity.getLockExpiredAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("해당 로켓의 잠금 해제일이 아직 지나지 않았습니다.");
        }

        // 잠금 해제 수행
        findEntity.setIsLock(false); // '잠금 해제' 상태로 명시적으로 설정
        rocketRepository.save(findEntity);
    }
}
