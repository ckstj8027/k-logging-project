package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
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
    private final AuthService authService;

    @GetMapping
    public String policies(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        // PolicyService에 getAllPoliciesByTenant(tenant) 메서드 추가 필요
        // 여기서는 일단 getAllPolicies().stream().filter()로 구현 (추후 Service 수정 권장)
        model.addAttribute("policies", policyService.getAllPolicies().stream()
                .filter(p -> p.getTenant().equals(tenant))
                .toList());
        return "policies/list";
    }
}
