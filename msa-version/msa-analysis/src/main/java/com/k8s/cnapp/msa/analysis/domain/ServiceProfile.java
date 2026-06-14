package com.k8s.cnapp.msa.analysis.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "service_profiles")
public class ServiceProfile extends BaseResourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String namespace;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "cluster_ip")
    private String clusterIp;

    @Column(name = "external_ips")
    private String externalIps;

    @OneToMany(mappedBy = "serviceProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicePortProfile> ports = new ArrayList<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ServiceProfile(Tenant tenant, String namespace, String name, String type, String clusterIp, String externalIps) {
        super(tenant);
        this.namespace = namespace;
        this.name = name;
        this.type = type;
        this.clusterIp = clusterIp;
        this.externalIps = externalIps;
    }

    public void addPort(ServicePortProfile port) {
        this.ports.add(port);
        port.setServiceProfile(this);
    }
}
