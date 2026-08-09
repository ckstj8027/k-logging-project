package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pod_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pod_profile_asset", columnNames = {"tenant_id", "namespace", "pod_name", "container_name"})
})
public class PodProfileEntity extends BaseResourceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pod_profile_seq")
    @SequenceGenerator(name = "pod_profile_seq", sequenceName = "pod_profile_seq", allocationSize = 50)
    private Long id;

    @Embedded
    private AssetContextEmbeddable assetContext;

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

    @Column(name = "cpu_usage")
    private String cpuUsage;

    @Column(name = "memory_usage")
    private String memoryUsage;


    static PodProfileEntity fromModel(PodProfile model, TenantEntity tenant) {
        PodProfileEntity entity = new PodProfileEntity();
        entity.tenant = tenant;
        entity.apply(model);
        return entity;
    }

    void apply(PodProfile model) {
        this.assetContext = AssetContextEmbeddable.fromModel(model.getAssetContext());
        this.privileged = model.getPrivileged();
        this.runAsUser = model.getRunAsUser();
        this.runAsRoot = model.getRunAsRoot();
        this.allowPrivilegeEscalation = model.getAllowPrivilegeEscalation();
        this.readOnlyRootFilesystem = model.getReadOnlyRootFilesystem();
        this.cpuUsage = model.getCpuUsage();
        this.memoryUsage = model.getMemoryUsage();
        this.lastSeenAt = model.getLastSeenAt();
    }

    PodProfile toModel(Tenant tenant) {
        return PodProfile.restore(id, tenant, assetContext.toModel(), privileged, runAsUser, runAsRoot, allowPrivilegeEscalation, readOnlyRootFilesystem, cpuUsage, memoryUsage, createdAt, lastSeenAt);
    }
}
