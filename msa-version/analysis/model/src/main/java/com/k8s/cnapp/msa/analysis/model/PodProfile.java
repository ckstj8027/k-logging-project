package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PodProfile extends BaseResourceProfile {

    private AssetContext assetContext;
    private Boolean privileged;
    private Long runAsUser;
    private Boolean runAsRoot;
    private Boolean allowPrivilegeEscalation;
    private Boolean readOnlyRootFilesystem;
    private String cpuUsage;
    private String memoryUsage;

    protected PodProfile() {
    }

    public PodProfile(Tenant tenant, AssetContext assetContext, Boolean privileged, Long runAsUser, Boolean allowPrivilegeEscalation, Boolean readOnlyRootFilesystem, String cpuUsage, String memoryUsage) {
        super(tenant);
        this.assetContext = assetContext;
        this.privileged = privileged;
        this.runAsUser = runAsUser;
        this.runAsRoot = (runAsUser != null && runAsUser == 0);
        this.allowPrivilegeEscalation = allowPrivilegeEscalation;
        this.readOnlyRootFilesystem = readOnlyRootFilesystem;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    public void update(AssetContext context, Boolean privileged, Long runAsUser, Boolean allowPrivilegeEscalation, Boolean readOnlyRootFilesystem) {
        this.assetContext = context;
        this.privileged = privileged;
        this.runAsUser = runAsUser;
        this.runAsRoot = (runAsUser != null && runAsUser == 0);
        this.allowPrivilegeEscalation = allowPrivilegeEscalation;
        this.readOnlyRootFilesystem = readOnlyRootFilesystem;
        updateLastSeenAt(LocalDateTime.now());
    }

    /** 영속성 어댑터 전용 복원 팩토리. */
    public static PodProfile restore(Long id, Tenant tenant, AssetContext assetContext, Boolean privileged, Long runAsUser, Boolean runAsRoot, Boolean allowPrivilegeEscalation, Boolean readOnlyRootFilesystem, String cpuUsage, String memoryUsage, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        PodProfile profile = new PodProfile();
        profile.restoreBase(id, tenant, createdAt, lastSeenAt);
        profile.assetContext = assetContext;
        profile.privileged = privileged;
        profile.runAsUser = runAsUser;
        profile.runAsRoot = runAsRoot;
        profile.allowPrivilegeEscalation = allowPrivilegeEscalation;
        profile.readOnlyRootFilesystem = readOnlyRootFilesystem;
        profile.cpuUsage = cpuUsage;
        profile.memoryUsage = memoryUsage;
        return profile;
    }
}
