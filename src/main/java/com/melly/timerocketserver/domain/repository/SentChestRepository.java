package com.melly.timerocketserver.domain.repository;

import com.melly.timerocketserver.domain.entity.SentChestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SentChestRepository extends JpaRepository<SentChestEntity,Long> {
    // 송신 보관함 조회 - 삭제x , 로켓의 송신자 id로
    Page<SentChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId, Pageable pageable);
    Page<SentChestEntity> findByIsDeletedFalseAndRocket_SenderUser_UserIdAndRocket_RocketNameContaining(Long userId, String rocketName, Pageable pageable);
    
    // 송신 보관함의 로켓 갯수 조회
    Long countByIsDeletedFalseAndRocket_SenderUser_UserId(Long userId);
    
    // 송신 보관함 조회 - 삭제x , 보관함 id, 로켓은 not null, 로켓의 송신자 id 로
    Optional<SentChestEntity> findByIsDeletedFalseAndSentChestIdAndRocket_SenderUser_UserId(Long sentChestId, Long userId);


}
