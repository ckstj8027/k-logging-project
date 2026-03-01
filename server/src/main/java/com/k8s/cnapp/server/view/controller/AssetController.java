package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final AuthService authService;

    @GetMapping("/pods")
    public String pods(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        model.addAttribute("pods", podProfileRepository.findAllByTenant(tenant));
        return "assets/pods";
    }

    @GetMapping("/services")
    public String services(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        model.addAttribute("services", serviceProfileRepository.findAllByTenantWithPorts(tenant));
        return "assets/services";
    }

    @GetMapping("/nodes")
    public String nodes(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        model.addAttribute("nodes", nodeProfileRepository.findAllByTenant(tenant));
        return "assets/nodes";
    }

    @GetMapping("/namespaces")
    public String namespaces(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        model.addAttribute("namespaces", namespaceProfileRepository.findAllByTenant(tenant));
        return "assets/namespaces";
    }

    @GetMapping("/deployments")
    public String deployments(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        model.addAttribute("deployments", deploymentProfileRepository.findAllByTenant(tenant));
        return "assets/deployments";
    }

    @GetMapping("/events")
    public String events(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        // 이벤트는 최신순으로 정렬 (Repository에 findAllByTenant 메서드에 Sort 파라미터 추가 필요하거나, 일단 findAllByTenant 후 정렬)
        // 여기서는 간단히 findAllByTenant 사용 (정렬은 추후 추가)
        model.addAttribute("events", eventProfileRepository.findAllByTenant(tenant));
        return "assets/events";
    }
}
