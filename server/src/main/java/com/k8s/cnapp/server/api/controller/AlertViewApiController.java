package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertViewApiController {

    private final AlertRepository alertRepository;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<?> alerts() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(alertRepository.findAll().stream()
                .filter(a -> a.getTenant().equals(tenant))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .toList());
    }
}
