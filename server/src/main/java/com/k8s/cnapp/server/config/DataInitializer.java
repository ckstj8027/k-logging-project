package com.k8s.cnapp.server.config;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.domain.User;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.auth.repository.UserRepository;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PolicyService policyService;

    @EventListener(ApplicationReadyEvent.class)
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
}
