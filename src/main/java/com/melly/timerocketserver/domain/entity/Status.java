package com.melly.timerocketserver.domain.entity;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("계정 활성화"),
    INACTIVE("계정 비활성화"),
    DELETED("계정 탈퇴");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
