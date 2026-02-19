package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.profile.domain.Profile;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AnomalyDetectionService {

    // 마할라노비스 거리(Mahalanobis Distance) 계산
    // 데이터의 분산과 상관관계를 고려한 거리 측정 방식
    public double calculateMahalanobisDistance(RealVector point, RealVector mean, RealMatrix covarianceMatrix) {
        RealVector diff = point.subtract(mean);
        // (x - μ)^T * Σ^-1 * (x - μ)
        // 역행렬 계산 필요 (특이 행렬일 경우 Pseudo-inverse 사용 고려)
        RealMatrix inverseCovariance = new org.apache.commons.math3.linear.LUDecomposition(covarianceMatrix).getSolver().getInverse();
        return Math.sqrt(diff.dotProduct(inverseCovariance.operate(diff)));
    }

    // 이상 탐지 수행
    public boolean detectAnomaly(Profile profile, RealVector baselineMean, RealMatrix baselineCovariance, double threshold) {
        // Profile -> RealVector 변환 로직 필요
        // RealVector point = convertToVector(profile);
        // double distance = calculateMahalanobisDistance(point, baselineMean, baselineCovariance);
        // return distance > threshold;
        return false; // 임시 반환
    }

    // 알림 생성 (Context 포함)
    public String generateAlert(Profile profile, double distance, double threshold) {
        return String.format(
                "Anomaly Detected! Asset: %s, Distance: %.2f (Threshold: %.2f). Context: %s",
                profile.getAssetContext().getAssetKey(),
                distance,
                threshold,
                profile.getFeatures().toString() // 어떤 피처가 튀었는지 확인 가능
        );
    }
}
