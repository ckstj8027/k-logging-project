package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.domain.User;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.auth.repository.UserRepository;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SignupApiController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PolicyService policyService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        
        if (tenantRepository.findByName(request.getCompanyName()).isPresent()) {
            return ResponseEntity.badRequest().body("Company name already exists");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // 1. Tenant 생성
        Tenant tenant = new Tenant(request.getCompanyName());
        tenantRepository.save(tenant);

        // 2. 기본 정책 생성
        policyService.createDefaultPoliciesForTenant(tenant);

        // 3. Admin User 생성
        User user = new User(request.getUsername(), "{noop}" + request.getPassword(), User.Role.ADMIN, tenant);
        userRepository.save(user);

        // 4. 가입 완료 응답 (API Key 반환)
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
