package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final AlertRepository alertRepository;
    private final AuthService authService;

    @GetMapping("/")
    public String dashboard(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        
        // count() 대신 countByTenant(tenant) 사용 필요 (Repository에 메서드 추가 필요)
        // 일단 findAllByTenant().size()로 대체하거나 Repository에 count 메서드 추가해야 함.
        // 성능을 위해 countByTenant 추가 권장.
        
        // 여기서는 일단 findAllByTenant().size()로 구현하고, 추후 최적화 가능
        model.addAttribute("podCount", podProfileRepository.findAllByTenant(tenant).size());
        model.addAttribute("serviceCount", serviceProfileRepository.findAllByTenantWithPorts(tenant).size());
        model.addAttribute("nodeCount", nodeProfileRepository.findAllByTenant(tenant).size());
        model.addAttribute("namespaceCount", namespaceProfileRepository.findAllByTenant(tenant).size());
        model.addAttribute("deploymentCount", deploymentProfileRepository.findAllByTenant(tenant).size());
        model.addAttribute("eventCount", eventProfileRepository.findAllByTenant(tenant).size());
        model.addAttribute("alertCount", alertRepository.findByStatus(com.k8s.cnapp.server.alert.domain.Alert.Status.OPEN).stream().filter(a -> a.getTenant().equals(tenant)).count()); // AlertRepository 수정 필요

        return "dashboard/index";
    }
}
