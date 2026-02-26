package com.k8s.cnapp.server.view.controller;

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

    @GetMapping("/pods")
    public String pods(Model model) {
        model.addAttribute("pods", podProfileRepository.findAll());
        return "assets/pods";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("services", serviceProfileRepository.findAllWithPorts());
        return "assets/services";
    }

    @GetMapping("/nodes")
    public String nodes(Model model) {
        model.addAttribute("nodes", nodeProfileRepository.findAll());
        return "assets/nodes";
    }

    @GetMapping("/namespaces")
    public String namespaces(Model model) {
        model.addAttribute("namespaces", namespaceProfileRepository.findAll());
        return "assets/namespaces";
    }

    @GetMapping("/deployments")
    public String deployments(Model model) {
        model.addAttribute("deployments", deploymentProfileRepository.findAll());
        return "assets/deployments";
    }

    @GetMapping("/events")
    public String events(Model model) {
        // 이벤트는 최신순으로 정렬
        model.addAttribute("events", eventProfileRepository.findAll(Sort.by(Sort.Direction.DESC, "lastTimestamp")));
        return "assets/events";
    }
}
