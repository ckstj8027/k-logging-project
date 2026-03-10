package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.dto.PolicyDto;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyViewApiController {

    private final PolicyService policyService;
    private final AuthService authService;
    private final com.k8s.cnapp.server.policy.repository.PolicyRepository policyRepository;

    @GetMapping
    public ResponseEntity<?> policies(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        
        return ResponseEntity.ok(policyRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(PolicyDto::new)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Tenant tenant = authService.getCurrentTenant();
        
        // 정책 소유권 확인
        Policy policy = policyService.getPolicyById(id);
        if (!policy.getTenant().equals(tenant)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        String value = (String) payload.get("value");
        Boolean enabled = (Boolean) payload.get("enabled");

        if (value == null || enabled == null) {
            return ResponseEntity.badRequest().body("Missing value or enabled field");
        }

        return ResponseEntity.ok(new PolicyDto(policyService.updatePolicy(id, value, enabled)));
    }
}
