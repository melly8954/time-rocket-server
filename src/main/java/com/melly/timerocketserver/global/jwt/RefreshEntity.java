package com.melly.timerocketserver.global.jwt;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="refresh_tbl")
public class RefreshEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name="refresh_token")
    private String refreshToken;

    @Column(name="token_expiration")
    private String tokenExpiration;

}
