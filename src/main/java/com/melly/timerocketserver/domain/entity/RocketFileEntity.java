package com.melly.timerocketserver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rocket_file_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RocketFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rocket_id")
    private RocketEntity rocket;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "unique_name")
    private String uniqueName;

    @Column(name = "saved_path")
    private String savedPath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = this.uploadedAt == null ? LocalDateTime.now() : this.uploadedAt;
    }
}
