package com.k8s.cnapp.server.profile.domain;

import com.k8s.cnapp.server.auth.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "service_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_profile", columnNames = {"tenant_id", "namespace", "name"})
})
public class ServiceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String namespace;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // ClusterIP, NodePort, LoadBalancer

    @Column(name = "cluster_ip")
    private String clusterIp;

    @Column(name = "external_ips")
    private String externalIps; // Comma separated

    // JSON 필드 대신 정규화된 테이블 사용
    @OneToMany(mappedBy = "serviceProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicePortProfile> ports = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ServiceProfile(Tenant tenant, String namespace, String name, String type, String clusterIp, String externalIps) {
        this.tenant = tenant;
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
    }

    public void addPort(ServicePortProfile port) {
        this.ports.add(port);
        port.setServiceProfile(this);
    }

    public void clearPorts() {
        this.ports.clear();
    }
}
