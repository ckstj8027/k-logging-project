package com.k8s.cnapp.msa.auth.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 테넌트 도메인 모델 (순수 POJO).
 */
public record Tenant(Long id, String name, String apiKey, LocalDateTime createdAt) {

    /** 기존 Tenant(name) 생성자와 동일하게 API Key 를 UUID 로 발급한다. */
    public static Tenant create(String name) {
        return new Tenant(null, name, UUID.randomUUID().toString(), null);
    }
}
