package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.profile.repository.PodProfileRepository;
import com.k8s.cnapp.server.profile.repository.ServiceProfileRepository;
import com.k8s.cnapp.server.profile.repository.NodeProfileRepository;
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
    private final AlertRepository alertRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("podCount", podProfileRepository.count());
        model.addAttribute("serviceCount", serviceProfileRepository.count());
        model.addAttribute("nodeCount", nodeProfileRepository.count());
        model.addAttribute("alertCount", alertRepository.count());
        return "dashboard/index";
    }
}
