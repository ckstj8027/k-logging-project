package com.k8s.cnapp.msa.analysis.service;

import com.k8s.cnapp.msa.analysis.model.ParsedSnapshot;

/**
 * In-Port: 파싱된 클러스터 스냅샷 분석 유스케이스.
 * 테넌트 확보 → 정책 컨텍스트 구성 → 프로파일 영속화 → 정책 평가/알림 저장 → 캐시 무효화.
 */
public interface ProcessSnapshotUseCase {

    void process(Long tenantId, ParsedSnapshot snapshot);
}
