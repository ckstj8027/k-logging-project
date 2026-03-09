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
@Table(name = "node_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_node_profile", columnNames = {"tenant_id", "name"})
}, indexes = {
        @Index(name = "idx_node_last_seen", columnList = "last_seen_at")
})
public class NodeProfile extends BaseResourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "node_profile_seq")
    @SequenceGenerator(name = "node_profile_seq", sequenceName = "node_profile_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "os_image")
    private String osImage;

    @Column(name = "kernel_version")
    private String kernelVersion;

    @Column(name = "container_runtime_version")
    private String containerRuntimeVersion;

    @Column(name = "kubelet_version")
    private String kubeletVersion;

    @Column(name = "cpu_capacity")
    private String cpuCapacity;

    @Column(name = "memory_capacity")
    private String memoryCapacity;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

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
    }
}
