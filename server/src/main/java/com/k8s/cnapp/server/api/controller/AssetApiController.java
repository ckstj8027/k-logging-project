package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.profile.dto.*;
import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

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
    public ResponseEntity<?> pods(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(podProfileRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(PodProfileDto::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/services")
    public ResponseEntity<?> services(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(serviceProfileRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(ServiceProfileDto::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/nodes")
    public ResponseEntity<?> nodes(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(nodeProfileRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(NodeProfileDto::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/namespaces")
    public ResponseEntity<?> namespaces(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(namespaceProfileRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(NamespaceProfileDto::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/deployments")
    public ResponseEntity<?> deployments(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(deploymentProfileRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(DeploymentProfileDto::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/events")
    public ResponseEntity<?> events(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(eventProfileRepository.findAllByTenantNoOffset(tenant, lastId, size).stream()
                .map(EventProfileDto::new)
                .collect(Collectors.toList()));
    }
}
