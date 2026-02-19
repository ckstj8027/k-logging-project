package com.k8s.cnapp.server.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "service_ports")
public class ServicePortProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_profile_id", nullable = false)
    private ServiceProfile serviceProfile;

    @Column(name = "name")
    private String name;

    @Column(name = "protocol")
    private String protocol; // TCP, UDP, SCTP

    @Column(name = "port")
    private Integer port;

    @Column(name = "target_port")
    private String targetPort; // IntOrString (e.g., "8080" or "http")

    @Column(name = "node_port")
    private Integer nodePort;

    public ServicePortProfile(String name, String protocol, Integer port, String targetPort, Integer nodePort) {
        this.name = name;
        this.protocol = protocol;
        this.port = port;
        this.targetPort = targetPort;
        this.nodePort = nodePort;
    }

    public void setServiceProfile(ServiceProfile serviceProfile) {
        this.serviceProfile = serviceProfile;
    }
}
