package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
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

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("podCount", podProfileRepository.count());
        model.addAttribute("serviceCount", serviceProfileRepository.count());
        model.addAttribute("nodeCount", nodeProfileRepository.count());
        model.addAttribute("namespaceCount", namespaceProfileRepository.count());
        model.addAttribute("deploymentCount", deploymentProfileRepository.count());
        model.addAttribute("eventCount", eventProfileRepository.count());
        model.addAttribute("alertCount", alertRepository.count());
        return "dashboard/index";
    }
}
