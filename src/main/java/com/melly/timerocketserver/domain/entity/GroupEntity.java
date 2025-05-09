package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_tbl")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private UserEntity leader;

    @Column(name = "is_private")
    private Boolean isPrivate;

    private String password;

    @Column(name = "background_image")
    private String backgroundImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
