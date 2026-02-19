package com.k8s.cnapp.server.profile.domain;

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
        @UniqueConstraint(name = "uk_node_profile", columnNames = {"name"})
})
public class NodeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public NodeProfile(String name, String osImage, String kernelVersion, String containerRuntimeVersion, String kubeletVersion, String cpuCapacity, String memoryCapacity) {
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
