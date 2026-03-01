package com.k8s.cnapp.server.profile.domain;

import com.k8s.cnapp.server.auth.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pod_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pod_profile_asset", columnNames = {"tenant_id", "namespace", "pod_name", "container_name"})
})
public class PodProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // --- Asset Context (자산 정보) ---
    @Embedded
    private AssetContext assetContext;

    // --- Security Context (보안 설정) ---
    @Column(name = "privileged")
    private Boolean privileged;

    @Column(name = "run_as_user")
    private Long runAsUser;

    @Column(name = "run_as_root")
    private Boolean runAsRoot; // runAsUser == 0 인 경우 true

    @Column(name = "allow_privilege_escalation")
    private Boolean allowPrivilegeEscalation;

    @Column(name = "read_only_root_filesystem")
    private Boolean readOnlyRootFilesystem;

    // --- Metadata ---
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public PodProfile(Tenant tenant, AssetContext assetContext,
                      Boolean privileged, Long runAsUser, Boolean allowPrivilegeEscalation, Boolean readOnlyRootFilesystem) {
        this.tenant = tenant;
        this.assetContext = assetContext;
        this.privileged = privileged;
        this.runAsUser = runAsUser;
        this.runAsRoot = (runAsUser != null && runAsUser == 0);
        this.allowPrivilegeEscalation = allowPrivilegeEscalation;
        this.readOnlyRootFilesystem = readOnlyRootFilesystem;
    }

    public void updateAssetContext(AssetContext newAssetContext) {
        this.assetContext = newAssetContext;
    }

    public void updateSecurityContext(Boolean privileged, Long runAsUser, Boolean allowPrivilegeEscalation, Boolean readOnlyRootFilesystem) {
        this.privileged = privileged;
        this.runAsUser = runAsUser;
        this.runAsRoot = (runAsUser != null && runAsUser == 0);
        this.allowPrivilegeEscalation = allowPrivilegeEscalation;
        this.readOnlyRootFilesystem = readOnlyRootFilesystem;
    }
}
