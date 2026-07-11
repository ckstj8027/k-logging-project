package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NamespaceProfile extends BaseResourceProfile {

    private String name;
    private String status;

    protected NamespaceProfile() {
    }

    public NamespaceProfile(Tenant tenant, String name, String status) {
        super(tenant);
        this.name = name;
        this.status = status;
    }

    public void update(String status) {
        this.status = status;
        updateLastSeenAt(LocalDateTime.now());
    }

    /** 영속성 어댑터 전용 복원 팩토리. */
    public static NamespaceProfile restore(Long id, Tenant tenant, String name, String status, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        NamespaceProfile profile = new NamespaceProfile();
        profile.restoreBase(id, tenant, createdAt, lastSeenAt);
        profile.name = name;
        profile.status = status;
        return profile;
    }
}
