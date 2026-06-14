package com.k8s.cnapp.msa.auth.controller;

import com.k8s.cnapp.msa.auth.domain.Tenant;
import com.k8s.cnapp.msa.auth.domain.User;
import com.k8s.cnapp.msa.auth.repository.TenantRepository;
import com.k8s.cnapp.msa.auth.repository.UserRepository;
import com.k8s.cnapp.msa.auth.service.AuthPolicyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SignupApiController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuthPolicyService policyService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        log.info("Received signup request for user: {} and company: {}", request.getUsername(), request.getCompanyName());
        
        if (tenantRepository.findByName(request.getCompanyName()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Company name already exists"));
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        Tenant tenant = new Tenant(request.getCompanyName());
        tenantRepository.save(tenant);

        policyService.createDefaultPoliciesForTenant(tenant);

        User user = new User(request.getUsername(), "{noop}" + request.getPassword(), User.Role.ADMIN, tenant);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("apiKey", tenant.getApiKey());
        return ResponseEntity.ok(response);
    }

    @Data
    public static class SignupRequest {
        private String companyName;
        private String username;
        private String password;
    }
}
