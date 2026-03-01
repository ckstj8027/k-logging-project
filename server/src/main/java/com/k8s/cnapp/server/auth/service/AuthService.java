package com.k8s.cnapp.server.auth.service;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.domain.User;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.auth.repository.UserRepository;
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

    @PostConstruct
    @Transactional
    public void initDefaultData() {
        // 1. Default Tenant 생성
        if (tenantRepository.count() == 0) {
            Tenant defaultTenant = new Tenant("Default Company");
            tenantRepository.save(defaultTenant);
            log.info("Created default tenant: {} (API Key: {})", defaultTenant.getName(), defaultTenant.getApiKey());

            // 2. Default Admin User 생성
            // 패스워드는 일단 평문으로 저장 (나중에 Security 설정 시 인코딩 적용 필요)
            User admin = new User("admin", "{noop}admin123", User.Role.ADMIN, defaultTenant);
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
