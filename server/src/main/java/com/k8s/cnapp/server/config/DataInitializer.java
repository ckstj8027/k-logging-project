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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PolicyService policyService;
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDefaultData() {
        // 0. ShedLock 테이블 자동 생성 (PostgreSQL 전용)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS shedlock (" +
                "name VARCHAR(64) NOT NULL, " +
                "lock_until TIMESTAMP NOT NULL, " +
                "locked_at TIMESTAMP NOT NULL, " +
                "locked_by VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (name))");
        log.info("Verified ShedLock table existence.");

        String defaultTenantName = "Default Company";
        
        // 1. Default Tenant 및 테스트용 테넌트 10개 생성
        for (int i = 1; i <= 10; i++) {
            final int index = i;
            String tenantName = (index == 1) ? "Default Company" : "Test Client " + index;
            
            tenantRepository.findByName(tenantName).orElseGet(() -> {
                Tenant newTenant = new Tenant(tenantName);
                tenantRepository.save(newTenant);
                log.info(">>>> [API KEY] Created tenant: '{}' | Key: {}", newTenant.getName(), newTenant.getApiKey());
                
                // 각 테넌트별 기본 정책 생성
                policyService.createDefaultPoliciesForTenant(newTenant);
                
                // 각 테넌트별 Admin 유저도 생성 (테스트 편의성)
                String username = "admin" + (index == 1 ? "" : index);
                if (userRepository.findByUsername(username).isEmpty()) {
                    User admin = new User(username, "{noop}admin123", User.Role.ADMIN, newTenant);
                    userRepository.save(admin);
                }
                return newTenant;
            });
        }
    }
}
