package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyViewController {

    private final PolicyService policyService;

    @GetMapping
    public String policies(Model model) {
        model.addAttribute("policies", policyService.getAllPolicies());
        return "policies/list";
    }
}
