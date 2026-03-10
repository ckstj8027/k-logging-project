package com.k8s.cnapp.server.auth.service;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.domain.User;
import com.k8s.cnapp.server.auth.repository.UserRepository;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    
    // API Key 캐시 (성능 최적화용)
    private final Map<String, Tenant> apiKeyCache = new ConcurrentHashMap<>();

    public Tenant getTenantByApiKey(String apiKey) {
        return apiKeyCache.computeIfAbsent(apiKey, key -> 
            tenantRepository.findByApiKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Invalid API Key"))
        );
    }

    public Tenant getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated authentication found");
        }

        Object principal = authentication.getPrincipal();

        // 1. API Key 인증인 경우 (ApiKeyAuthFilter에서 Tenant 객체를 직접 넣음)
        if (principal instanceof Tenant) {
            return (Tenant) principal;
        }

        // 2. 일반 로그인(JWT) 인증인 경우 (Principal이 UserDetails임)
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .map(User::getTenant)
                    .orElseThrow(() -> new IllegalStateException("User tenant not found: " + username));
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
    }
}
