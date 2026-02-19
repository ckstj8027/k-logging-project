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
@Table(name = "service_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_profile", columnNames = {"namespace", "name"})
})
public class ServiceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "ports_json", columnDefinition = "TEXT")
    private String portsJson; // JSON string of ports

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ServiceProfile(String namespace, String name, String type, String clusterIp, String externalIps, String portsJson) {
        this.namespace = namespace;
        this.name = name;
        this.type = type;
        this.clusterIp = clusterIp;
        this.externalIps = externalIps;
        this.portsJson = portsJson;
    }

    public void update(String type, String clusterIp, String externalIps, String portsJson) {
        this.type = type;
        this.clusterIp = clusterIp;
        this.externalIps = externalIps;
        this.portsJson = portsJson;
    }
}
