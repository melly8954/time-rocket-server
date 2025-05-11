package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="rocket_tbl")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rocket_id")
    private Long rocketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiverUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    private String name;

    private String design;

    @Column(name = "is_lock")
    private Boolean isLock;

    @Column(name = "lock_expired_at")
    private LocalDateTime lockExpiredAt;

    @Column(name = "receiver_type")
    private String receiverType;

    private String content;

    @Column(name = "is_temp")
    private Boolean isTemp;

    @Column(name="temp_created_at")
    private LocalDateTime tempCreatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
