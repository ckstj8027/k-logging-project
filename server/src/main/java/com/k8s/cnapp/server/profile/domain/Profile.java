package com.k8s.cnapp.server.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_asset", columnNames = {"namespace", "pod_name", "container_name"})
})
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Asset Context (자산 정보) ---
    // 어떤 리소스에서 발생한 프로필인지 식별
    @Embedded
    private AssetContext assetContext;

    // --- Feature Vector (통계적 특징) ---
    // 다차원 공간의 한 점 (예: [연결 수, 패킷 크기 평균, 에러율, ...])
    // JSON 형태로 저장하거나 별도 테이블로 분리할 수 있음. 여기서는 단순화를 위해 ElementCollection 사용
    @ElementCollection
    @CollectionTable(name = "profile_features", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "feature_name")
    @Column(name = "feature_value")
    private Map<String, Double> features;

    // --- Metadata ---
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 학습용인지, 실제 탐지용인지 구분 (나중에 베이스라인 구축 시 사용)
    @Enumerated(EnumType.STRING)
    private ProfileType type;

    public Profile(AssetContext assetContext, Map<String, Double> features, ProfileType type) {
        this.assetContext = assetContext;
        this.features = features;
        this.type = type;
    }

    public void updateFeatures(Map<String, Double> newFeatures) {
        this.features.clear();
        this.features.putAll(newFeatures);
    }

    public void updateAssetContext(AssetContext newAssetContext) {
        this.assetContext = newAssetContext;
    }

    public enum ProfileType {
        LEARNING, // 학습 데이터 (베이스라인 구축용)
        INFERENCE // 추론 데이터 (이상 탐지용)
    }
}
