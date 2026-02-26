package com.k8s.cnapp.server.policy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_rule", columnNames = {"resource_type", "rule_type"})
})
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(name = "value", nullable = false)
    private String value; // 콤마로 구분된 리스트나 단일 값 (예: "22,3306", "8", "true")

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "description")
    private String description;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Policy(ResourceType resourceType, RuleType ruleType, String value, String description) {
        this.resourceType = resourceType;
        this.ruleType = ruleType;
        this.value = value;
        this.description = description;
    }

    public void update(String value, boolean enabled) {
        this.value = value;
        this.enabled = enabled;
    }

    public enum ResourceType {
        POD, SERVICE, DEPLOYMENT, NODE
    }

    public enum RuleType {
        // Pod
        PRIVILEGED_DENY, // true/false (true면 privileged 허용 안 함)
        RUN_AS_ROOT_DENY, // true/false
        IMAGE_LATEST_TAG_DENY, // true/false

        // Service
        PORT_BLACKLIST, // "22,3306,..."
        EXTERNAL_IP_DENY, // true/false (LoadBalancer/NodePort 허용 안 함)

        // Deployment
        REPLICA_MAX_LIMIT, // "10"
        REPLICA_MIN_LIMIT, // "1"

        // Node
        CPU_LIMIT_CORES, // "8"
        MEMORY_LIMIT_BYTES // "34359738368" (32GB)
    }
}
