package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="sent_chest_tbl")
@Entity
public class SentChestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sent_chest_id")
    private Long sentChestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rocket_id")
    private RocketEntity rocket;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
