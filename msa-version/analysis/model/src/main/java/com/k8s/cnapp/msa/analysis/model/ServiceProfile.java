package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ServiceProfile extends BaseResourceProfile {

    private String namespace;
    private String name;
    private String type;
    private String clusterIp;
    private String externalIps;
    private final List<ServicePortProfile> ports = new ArrayList<>();

    protected ServiceProfile() {
    }

    public ServiceProfile(Tenant tenant, String namespace, String name, String type, String clusterIp, String externalIps) {
        super(tenant);
        this.namespace = namespace;
        this.name = name;
        this.type = type;
        this.clusterIp = clusterIp;
        this.externalIps = externalIps;
    }

    public void update(String type, String clusterIp, String externalIps) {
        this.type = type;
        this.clusterIp = clusterIp;
        this.externalIps = externalIps;
        updateLastSeenAt(LocalDateTime.now());
    }

    public void addPort(ServicePortProfile port) {
        this.ports.add(port);
        port.setServiceProfile(this);
    }

    /** 영속성 어댑터 전용 복원 팩토리. */
    public static ServiceProfile restore(Long id, Tenant tenant, String namespace, String name, String type, String clusterIp, String externalIps, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        ServiceProfile profile = new ServiceProfile();
        profile.restoreBase(id, tenant, createdAt, lastSeenAt);
        profile.namespace = namespace;
        profile.name = name;
        profile.type = type;
        profile.clusterIp = clusterIp;
        profile.externalIps = externalIps;
        return profile;
    }
}
