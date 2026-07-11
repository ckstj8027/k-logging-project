package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.model.ServiceProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기존 query 도메인의 ServiceProfile 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "service_profiles")
public class ServiceProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_profile_seq")
    @SequenceGenerator(name = "service_profile_seq", sequenceName = "service_profile_seq", allocationSize = 50)
    private Long id;
    private Long tenantId;
    private String namespace;
    private String name;
    private String type;
    private String clusterIp;

    public ServiceProfile toModel() {
        return new ServiceProfile(id, tenantId, namespace, name, type, clusterIp);
    }
}
