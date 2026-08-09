package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.model.NodeProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기존 query 도메인의 NodeProfile 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "node_profiles")
public class NodeProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "node_profile_seq")
    @SequenceGenerator(name = "node_profile_seq", sequenceName = "node_profile_seq", allocationSize = 50)
    private Long id;
    private Long tenantId;
    private String name;
    private String osImage;
    private String kernelVersion;
    private String containerRuntimeVersion;
    private String kubeletVersion;
    private String cpuCapacity;
    private String memoryCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;

    public NodeProfile toModel() {
        return new NodeProfile(id, tenantId, name, osImage, kernelVersion, containerRuntimeVersion,
                kubeletVersion, cpuCapacity, memoryCapacity, createdAt, lastSeenAt);
    }
}
