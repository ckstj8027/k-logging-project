package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetApiController {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final AuthService authService;

    @GetMapping("/pods")
    public ResponseEntity<?> pods() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(podProfileRepository.findAllByTenant(tenant));
    }

    @GetMapping("/services")
    public ResponseEntity<?> services() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(serviceProfileRepository.findAllByTenantWithPorts(tenant));
    }

    @GetMapping("/nodes")
    public ResponseEntity<?> nodes() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(nodeProfileRepository.findAllByTenant(tenant));
    }

    @GetMapping("/namespaces")
    public ResponseEntity<?> namespaces() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(namespaceProfileRepository.findAllByTenant(tenant));
    }

    @GetMapping("/deployments")
    public ResponseEntity<?> deployments() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(deploymentProfileRepository.findAllByTenant(tenant));
    }

    @GetMapping("/events")
    public ResponseEntity<?> events() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(eventProfileRepository.findAllByTenant(tenant));
    }
}
