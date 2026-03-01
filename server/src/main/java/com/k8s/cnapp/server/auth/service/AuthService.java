package com.k8s.cnapp.server.auth.service;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.domain.User;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.auth.repository.UserRepository;
import com.k8s.cnapp.server.policy.service.PolicyService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PolicyService policyService;

    @PostConstruct
    @Transactional
    public void initDefaultData() {
        String defaultTenantName = "Default Company";
        
        // 1. Default Tenant 생성 (이름으로 중복 체크)
        Tenant defaultTenant = tenantRepository.findByName(defaultTenantName).orElseGet(() -> {
            Tenant newTenant = new Tenant(defaultTenantName);
            tenantRepository.save(newTenant);
            log.info("Created default tenant: {} (API Key: {})", newTenant.getName(), newTenant.getApiKey());
            
            // 신규 생성 시에만 정책 생성
            policyService.createDefaultPoliciesForTenant(newTenant);
            return newTenant;
        });

        // 2. Default Admin User 생성 (이름으로 중복 체크)
        String adminUsername = "admin";
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User(adminUsername, "{noop}admin123", User.Role.ADMIN, defaultTenant);
            userRepository.save(admin);
            log.info("Created default admin user: {}", admin.getUsername());
        }
    }

    public Tenant getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userRepository.findByUsername(username)
                    .map(User::getTenant)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
