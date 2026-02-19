package com.k8s.cnapp.server.baseline.service;

import com.k8s.cnapp.server.profile.domain.Profile;
import com.k8s.cnapp.server.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BaselineService {

    private final ProfileRepository profileRepository;

    // 자산별(Namespace/Deployment) 베이스라인 상태 관리
    // Key: AssetKey (namespace/deployment), Value: BaselineStatus
    private final Map<String, BaselineStatus> baselineStatusMap = new ConcurrentHashMap<>();

    // 학습된 통계 정보 (평균, 공분산 등) - 메모리에 캐싱 (실제로는 DB 저장 필요)
    private final Map<String, MultivariateSummaryStatistics> baselineStatsMap = new ConcurrentHashMap<>();

    @Transactional
    public void addProfile(Profile profile) {
        String assetKey = profile.getAssetContext().getAssetKey();
        BaselineStatus status = baselineStatusMap.getOrDefault(assetKey, BaselineStatus.LEARNING);

        if (status == BaselineStatus.LEARNING) {
            // 학습 중: DB에 저장하고 통계 업데이트 (실시간 업데이트 or 배치 처리)
            profileRepository.save(profile);
            updateStatistics(assetKey, profile);
        } else {
            // 학습 완료: 이상 탐지 로직으로 전달 (여기서는 저장만 하거나 무시)
            // InferenceService.detect(profile, baselineStatsMap.get(assetKey));
        }
    }

    public void finishLearning(String assetKey) {
        baselineStatusMap.put(assetKey, BaselineStatus.FIXED);
        // 여기서 최종 통계값(평균, 공분산 행렬 등)을 영구 저장소에 기록해야 함.
    }

    private void updateStatistics(String assetKey, Profile profile) {
        // Apache Commons Math 등을 사용하여 실시간 통계 갱신
        // 실제 구현 시에는 차원 수에 맞는 벡터 연산 필요
    }

    public enum BaselineStatus {
        LEARNING, // 학습 중 (데이터 수집 및 분포 형성)
        FIXED     // 학습 완료 (베이스라인 고정, 이상 탐지 수행)
    }
}
