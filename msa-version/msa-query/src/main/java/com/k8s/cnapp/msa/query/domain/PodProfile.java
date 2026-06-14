package com.k8s.cnapp.msa.query.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pod_profiles")
public class PodProfile {
    @Id
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    private String namespace;
    
    @Column(name = "pod_name")
    private String podName;

    @Column(name = "container_name")
    private String containerName;

    private String image;
    private String status;
    
    @Column(name = "node_name")
    private String nodeName;

    @Column(name = "privileged")
    private Boolean privileged;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}
