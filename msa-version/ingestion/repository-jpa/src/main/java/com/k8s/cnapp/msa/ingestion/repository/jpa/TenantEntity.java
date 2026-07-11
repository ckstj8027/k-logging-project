package com.k8s.cnapp.msa.ingestion.repository.jpa;

import com.k8s.cnapp.msa.ingestion.model.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tenants")
public class TenantEntity {

    @Id
    private Long id;

    @Column(name = "api_key")
    private String apiKey;

    public Tenant toModel() {
        return new Tenant(id, apiKey);
    }
}
