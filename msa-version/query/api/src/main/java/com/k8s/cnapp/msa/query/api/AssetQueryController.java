package com.k8s.cnapp.msa.query.api;

import com.k8s.cnapp.msa.query.model.Alert;
import com.k8s.cnapp.msa.query.model.DeploymentProfile;
import com.k8s.cnapp.msa.query.model.EventProfile;
import com.k8s.cnapp.msa.query.model.NamespaceProfile;
import com.k8s.cnapp.msa.query.model.NodeProfile;
import com.k8s.cnapp.msa.query.model.PodProfile;
import com.k8s.cnapp.msa.query.model.Policy;
import com.k8s.cnapp.msa.query.model.ServiceProfile;
import com.k8s.cnapp.msa.query.service.AlertQueryUseCase;
import com.k8s.cnapp.msa.query.service.AssetQueryUseCase;
import com.k8s.cnapp.msa.query.service.DashboardQueryUseCase;
import com.k8s.cnapp.msa.query.service.PolicyQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 기존 AssetQueryController 와 동일한 URL/응답 형태. 이제 UseCase 만 호출한다.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssetQueryController {

    private final DashboardQueryUseCase dashboardQueryUseCase;
    private final AssetQueryUseCase assetQueryUseCase;
    private final AlertQueryUseCase alertQueryUseCase;
    private final PolicyQueryUseCase policyQueryUseCase;

    private Long getCurrentTenantId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtProvider.CustomPrincipal) {
            return ((JwtProvider.CustomPrincipal) principal).tenantId();
        }
        return 1L; // Fallback
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Long tenantId = getCurrentTenantId();
        log.info("Dashboard summary request for tenant: {}", tenantId);

        return ResponseEntity.ok(dashboardQueryUseCase.getSummary(tenantId));
    }

    @GetMapping("/assets/pods")
    public ResponseEntity<List<PodProfile>> getPods(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assetQueryUseCase.getPods(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/assets/nodes")
    public ResponseEntity<List<NodeProfile>> getNodes(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assetQueryUseCase.getNodes(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/assets/services")
    public ResponseEntity<List<ServiceProfile>> getServices(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assetQueryUseCase.getServices(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/assets/deployments")
    public ResponseEntity<List<DeploymentProfile>> getDeployments(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assetQueryUseCase.getDeployments(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/assets/namespaces")
    public ResponseEntity<List<NamespaceProfile>> getNamespaces(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assetQueryUseCase.getNamespaces(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/assets/events")
    public ResponseEntity<List<EventProfile>> getEvents(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assetQueryUseCase.getEvents(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(alertQueryUseCase.getOpenAlerts(getCurrentTenantId(), lastId, size));
    }

    @GetMapping("/policies")
    public ResponseEntity<List<Policy>> getPolicies() {
        return ResponseEntity.ok(policyQueryUseCase.getPolicies(getCurrentTenantId()));
    }
}
