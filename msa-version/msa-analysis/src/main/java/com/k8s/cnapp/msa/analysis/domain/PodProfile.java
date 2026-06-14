package com.k8s.cnapp.msa.analysis.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pod_profiles")
public class PodProfile extends BaseResourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private AssetContext assetContext;

    @Column(name = "privileged")
    private Boolean privileged;

    @Column(name = "run_as_user")
    private Long runAsUser;

    @Column(name = "run_as_root")
    private Boolean runAsRoot;

    @Column(name = "allow_privilege_escalation")
    private Boolean allowPrivilegeEscalation;

    @Column(name = "read_only_root_filesystem")
    private Boolean readOnlyRootFilesystem;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public PodProfile(Tenant tenant, AssetContext assetContext,
                      Boolean privileged, Long runAsUser, Boolean allowPrivilegeEscalation, Boolean readOnlyRootFilesystem) {
        super(tenant);
        this.assetContext = assetContext;
        this.privileged = privileged;
        this.runAsUser = runAsUser;
        this.runAsRoot = (runAsUser != null && runAsUser == 0);
        this.allowPrivilegeEscalation = allowPrivilegeEscalation;
        this.readOnlyRootFilesystem = readOnlyRootFilesystem;
    }
}
