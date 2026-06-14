package com.k8s.cnapp.msa.query.controller;

import com.k8s.cnapp.msa.query.domain.Alert;
import com.k8s.cnapp.msa.query.domain.PodProfile;
import com.k8s.cnapp.msa.query.repository.PodProfileQueryRepository;
import com.k8s.cnapp.msa.query.service.AlertQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class AssetQueryController {

    private final AlertQueryService alertQueryService;
    private final PodProfileQueryRepository podRepository;

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts(@RequestParam Long tenantId) {
        log.info("Dashboard request: Alerts for tenant {}", tenantId);
        return ResponseEntity.ok(alertQueryService.getOpenAlerts(tenantId));
    }

    @GetMapping("/pods")
    public ResponseEntity<List<PodProfile>> getPods(@RequestParam Long tenantId) {
        log.info("Dashboard request: Pods for tenant {}", tenantId);
        List<PodProfile> pods = podRepository.findAllByTenantId(tenantId);
        return ResponseEntity.ok(pods);
    }
}
