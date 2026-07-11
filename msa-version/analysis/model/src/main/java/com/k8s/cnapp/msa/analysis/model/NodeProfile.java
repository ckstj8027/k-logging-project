package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NodeProfile extends BaseResourceProfile {

    private String name;
    private String osImage;
    private String kernelVersion;
    private String containerRuntimeVersion;
    private String kubeletVersion;
    private String cpuCapacity;
    private String memoryCapacity;

    protected NodeProfile() {
    }

    public NodeProfile(Tenant tenant, String name, String osImage, String kernelVersion, String containerRuntimeVersion, String kubeletVersion, String cpuCapacity, String memoryCapacity) {
        super(tenant);
        this.name = name;
        this.osImage = osImage;
        this.kernelVersion = kernelVersion;
        this.containerRuntimeVersion = containerRuntimeVersion;
        this.kubeletVersion = kubeletVersion;
        this.cpuCapacity = cpuCapacity;
        this.memoryCapacity = memoryCapacity;
    }

    public void update(String osImage, String kernelVersion, String containerRuntimeVersion, String kubeletVersion, String cpuCapacity, String memoryCapacity) {
        this.osImage = osImage;
        this.kernelVersion = kernelVersion;
        this.containerRuntimeVersion = containerRuntimeVersion;
        this.kubeletVersion = kubeletVersion;
        this.cpuCapacity = cpuCapacity;
        this.memoryCapacity = memoryCapacity;
        updateLastSeenAt(LocalDateTime.now());
    }

    /** 영속성 어댑터 전용 복원 팩토리. */
    public static NodeProfile restore(Long id, Tenant tenant, String name, String osImage, String kernelVersion, String containerRuntimeVersion, String kubeletVersion, String cpuCapacity, String memoryCapacity, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        NodeProfile profile = new NodeProfile();
        profile.restoreBase(id, tenant, createdAt, lastSeenAt);
        profile.name = name;
        profile.osImage = osImage;
        profile.kernelVersion = kernelVersion;
        profile.containerRuntimeVersion = containerRuntimeVersion;
        profile.kubeletVersion = kubeletVersion;
        profile.cpuCapacity = cpuCapacity;
        profile.memoryCapacity = memoryCapacity;
        return profile;
    }
}
