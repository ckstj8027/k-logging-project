package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.NodeProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import jakarta.persistence.Column;
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
@Table(name = "node_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_node_profile", columnNames = {"tenant_id", "name"})
})
public class NodeProfileEntity extends BaseResourceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "node_profile_seq")
    @SequenceGenerator(name = "node_profile_seq", sequenceName = "node_profile_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String osImage;
    private String kernelVersion;
    private String containerRuntimeVersion;
    private String kubeletVersion;
    private String cpuCapacity;
    private String memoryCapacity;


    static NodeProfileEntity fromModel(NodeProfile model, TenantEntity tenant) {
        NodeProfileEntity entity = new NodeProfileEntity();
        entity.tenant = tenant;
        entity.name = model.getName();
        entity.apply(model);
        return entity;
    }

    void apply(NodeProfile model) {
        this.osImage = model.getOsImage();
        this.kernelVersion = model.getKernelVersion();
        this.containerRuntimeVersion = model.getContainerRuntimeVersion();
        this.kubeletVersion = model.getKubeletVersion();
        this.cpuCapacity = model.getCpuCapacity();
        this.memoryCapacity = model.getMemoryCapacity();
        this.lastSeenAt = model.getLastSeenAt();
    }

    NodeProfile toModel(Tenant tenant) {
        return NodeProfile.restore(id, tenant, name, osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpuCapacity, memoryCapacity, createdAt, lastSeenAt);
    }
}
