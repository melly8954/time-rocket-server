package com.melly.timerocketserver.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="user_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long userId;

    private String email;
    private String password;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    public String getRoleDescription() {
        return role.getDescription();
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public String getStatusDescription(){
        return status.getDescription();
    }

    @Column(name="created_date")
    private LocalDateTime createdDate;

    @Column(name="updated_date")
    private LocalDateTime updatedDate;

    @Column(name="deleted_date")
    private LocalDateTime deletedDate;

    private String provider;
    @Column(name="provider_id")
    private String providerId;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

}
