package com.k8s.cnapp.server.auth.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TenantRepository tenantRepository;
    private final PolicyService policyService;

    @PostMapping("/tenants")
    public ResponseEntity<Map<String, String>> createTenant(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant name is required"));
        }

        if (tenantRepository.findByName(name).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant name already exists"));
        }

        Tenant tenant = new Tenant(name);
        tenantRepository.save(tenant);
        
        // 신규 Tenant에 대한 기본 정책 생성
        policyService.createDefaultPoliciesForTenant(tenant);

        return ResponseEntity.ok(Map.of(
                "message", "Tenant created successfully",
                "apiKey", tenant.getApiKey()
        ));
    }
}
