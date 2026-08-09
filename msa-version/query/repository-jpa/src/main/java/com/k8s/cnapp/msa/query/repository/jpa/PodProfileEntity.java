package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.model.PodProfile;
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
 * 기존 query 도메인의 PodProfile 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pod_profiles")
public class PodProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pod_profile_seq")
    @SequenceGenerator(name = "pod_profile_seq", sequenceName = "pod_profile_seq", allocationSize = 50)
    private Long id;
    private Long tenantId;

    private String namespace;
    private String podName;
    private String containerName;
    private String image;
    private String status;
    private String podIp;
    private String nodeName;

    private Boolean privileged;
    private Long runAsUser;
    private Boolean runAsRoot;
    private Boolean allowPrivilegeEscalation;
    private Boolean readOnlyRootFilesystem;

    private String cpuUsage;
    private String memoryUsage;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;

    public PodProfile toModel() {
        return new PodProfile(id, tenantId, namespace, podName, containerName, image, status, podIp, nodeName,
                privileged, runAsUser, runAsRoot, allowPrivilegeEscalation, readOnlyRootFilesystem,
                cpuUsage, memoryUsage, createdAt, lastSeenAt);
    }
}
