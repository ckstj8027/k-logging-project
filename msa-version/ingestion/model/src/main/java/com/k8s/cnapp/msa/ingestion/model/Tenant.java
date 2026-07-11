package com.k8s.cnapp.msa.ingestion.model;

/**
 * 수집 관점의 테넌트 도메인 모델 (순수 POJO).
 */
public record Tenant(Long id, String apiKey) {
}
