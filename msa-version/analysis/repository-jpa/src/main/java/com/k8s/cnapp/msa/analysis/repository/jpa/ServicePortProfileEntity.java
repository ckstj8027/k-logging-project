package com.k8s.cnapp.msa.analysis.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "service_ports")
public class ServicePortProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_port_profile_seq")
    @SequenceGenerator(name = "service_port_profile_seq", sequenceName = "service_port_profile_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_profile_id", nullable = false)
    private ServiceProfileEntity serviceProfile;

    @Column(name = "name")
    private String name;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "port")
    private Integer port;

    @Column(name = "target_port")
    private String targetPort;

    @Column(name = "node_port")
    private Integer nodePort;

    ServicePortProfileEntity(String name, String protocol, Integer port, String targetPort, Integer nodePort) {
        this.name = name;
        this.protocol = protocol;
        this.port = port;
        this.targetPort = targetPort;
        this.nodePort = nodePort;
    }

    void setServiceProfile(ServiceProfileEntity serviceProfile) {
        this.serviceProfile = serviceProfile;
    }
}
