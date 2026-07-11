package com.k8s.cnapp.msa.auth.model;

import java.time.LocalDateTime;

/**
 * 사용자 도메인 모델 (순수 POJO).
 */
public record User(Long id, String username, String password, Role role, Long tenantId, LocalDateTime createdAt) {

    /** 기존 User(username, password, role, tenant) 생성자에 대응한다. */
    public static User create(String username, String password, Role role, Long tenantId) {
        return new User(null, username, password, role, tenantId, null);
    }

    public enum Role {
        ADMIN, USER
    }
}
