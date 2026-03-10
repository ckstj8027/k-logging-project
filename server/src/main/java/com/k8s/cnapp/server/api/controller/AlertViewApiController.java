package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.alert.dto.AlertDto;
import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertViewApiController {

    private final AlertRepository alertRepository;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<?> alerts(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        
        return ResponseEntity.ok(alertRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(AlertDto::new)
                .collect(Collectors.toList()));
    }
}
