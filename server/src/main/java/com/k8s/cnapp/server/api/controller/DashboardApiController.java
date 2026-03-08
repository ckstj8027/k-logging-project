package com.k8s.cnapp.server.api.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final AlertRepository alertRepository;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<?> dashboard() {
        Tenant tenant = authService.getCurrentTenant();
        Map<String, Object> response = new HashMap<>();
        
        response.put("podCount", podProfileRepository.findAllByTenant(tenant).size());
        response.put("serviceCount", serviceProfileRepository.findAllByTenantWithPorts(tenant).size());
        response.put("nodeCount", nodeProfileRepository.findAllByTenant(tenant).size());
        response.put("namespaceCount", namespaceProfileRepository.findAllByTenant(tenant).size());
        response.put("deploymentCount", deploymentProfileRepository.findAllByTenant(tenant).size());
        response.put("eventCount", eventProfileRepository.findAllByTenant(tenant).size());
        response.put("alertCount", alertRepository.findByStatus(com.k8s.cnapp.server.alert.domain.Alert.Status.OPEN).stream().filter(a -> a.getTenant().equals(tenant)).count());

        return ResponseEntity.ok(response);
    }
}
