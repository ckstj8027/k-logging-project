package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.profile.repository.PodProfileRepository;
import com.k8s.cnapp.server.profile.repository.ServiceProfileRepository;
import lombok.RequiredArgsConstructor;
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
}
