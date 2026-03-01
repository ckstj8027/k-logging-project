package com.k8s.cnapp.server.auth.filter;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");

        // Ingestion API 요청인 경우에만 로그 출력 (너무 많은 로그 방지)
        boolean isIngestionApi = request.getRequestURI().startsWith("/api/v1/ingestion");

        if (apiKey != null) {
            if (isIngestionApi) {
                log.debug("API Key received: {}", apiKey);
            }
            Tenant tenant = tenantRepository.findByApiKey(apiKey).orElse(null);
            if (tenant != null) {
                if (isIngestionApi) {
                    log.debug("Tenant authenticated: {}", tenant.getName());
                }
                // API Key가 유효하면 인증 객체 생성 (Principal에 Tenant 객체 저장)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        tenant, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                if (isIngestionApi) {
                    log.warn("Invalid API Key: {}", apiKey);
                }
            }
        } else {
            if (isIngestionApi) {
                log.warn("No API Key found in request header");
            }
        }

        filterChain.doFilter(request, response);
    }
}
