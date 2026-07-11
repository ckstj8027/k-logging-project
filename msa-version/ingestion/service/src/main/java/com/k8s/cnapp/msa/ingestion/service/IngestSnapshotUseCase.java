package com.k8s.cnapp.msa.ingestion.service;

/**
 * In-Port: 클러스터 스냅샷 수집 유스케이스.
 */
public interface IngestSnapshotUseCase {

    /**
     * @throws com.k8s.cnapp.msa.ingestion.exception.InvalidApiKeyException apiKey 가 유효하지 않은 경우
     */
    void ingest(String apiKey, String rawData);
}
