package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.ServiceProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "service_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_profile", columnNames = {"tenant_id", "namespace", "name"})
})
public class ServiceProfileEntity extends BaseResourceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_profile_seq")
    @SequenceGenerator(name = "service_profile_seq", sequenceName = "service_profile_seq", allocationSize = 50)
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
    private List<ServicePortProfileEntity> ports = new ArrayList<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    static ServiceProfileEntity fromModel(ServiceProfile model, TenantEntity tenant) {
        ServiceProfileEntity entity = new ServiceProfileEntity();
        entity.tenant = tenant;
        entity.namespace = model.getNamespace();
        entity.name = model.getName();
        entity.apply(model);
        return entity;
    }

    void apply(ServiceProfile model) {
        this.type = model.getType();
        this.clusterIp = model.getClusterIp();
        this.externalIps = model.getExternalIps();
        this.lastSeenAt = model.getLastSeenAt();
    }

    void addPort(ServicePortProfileEntity port) {
        this.ports.add(port);
        port.setServiceProfile(this);
    }

    /**
     * ports 는 기존 서비스에서도 쓰기/평가 경로에 사용되지 않으므로
     * LAZY 컬렉션을 로딩하지 않기 위해 매핑하지 않는다 (동작/SQL 보존).
     */
    ServiceProfile toModel(Tenant tenant) {
        return ServiceProfile.restore(id, tenant, namespace, name, type, clusterIp, externalIps, createdAt, lastSeenAt);
    }
}
