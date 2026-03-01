package com.k8s.cnapp.server.policy.controller;

import com.k8s.cnapp.server.policy.dto.PolicyDto;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public List<PolicyDto> getAllPolicies() {
        return policyService.getAllPolicies().stream()
                .map(PolicyDto::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyDto> updatePolicy(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String value = (String) payload.get("value");
        Boolean enabled = (Boolean) payload.get("enabled");
        
        if (value == null || enabled == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(new PolicyDto(policyService.updatePolicy(id, value, enabled)));
    }
}
