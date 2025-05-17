package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chest_tbl")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chest_id")
    private Long chestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rocket_id")
    private RocketEntity rocket;

    @Column(name = "is_public")
    private Boolean isPublic;  // tinyint(1) -> Boolean 처리

    @Column(name = "public_at")
    private LocalDateTime publicAt;

    @Column(name = "chest_location")
    private String chestLocation;

    @Column(name = "display_location")
    private String displayLocation;

    @Column(name = "is_deleted")
    private Boolean isDeleted;  // tinyint(1) -> Boolean 처리

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
