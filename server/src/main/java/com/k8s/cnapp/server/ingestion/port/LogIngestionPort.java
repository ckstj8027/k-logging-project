package com.k8s.cnapp.server.ingestion.port;

import com.k8s.cnapp.server.profile.domain.PodProfile;

/**
 * 외부(Agent, Kafka 등)로부터 로그 데이터를 받아 처리하는 포트.
 * 구현체는 HTTP Controller, Kafka Consumer 등이 될 수 있음.
 */
public interface LogIngestionPort {

    /**
     * 원본 로그(또는 이미 가공된 프로필)를 수신하여 처리.
     * @param rawData 원본 데이터 (JSON String 등)
     */
    void ingest(String rawData);

    /**
     * 이미 가공된 프로필 객체를 수신하여 처리.
     * (Agent에서 1차 가공 후 전송하는 경우)
     * @param profile 프로필 객체
     */
    void ingest(PodProfile profile);
}
