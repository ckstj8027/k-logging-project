package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertViewController {

    private final AlertRepository alertRepository;
    private final AuthService authService;

    @GetMapping
    public String alerts(Model model) {
        Tenant tenant = authService.getCurrentTenant();
        // findAllByTenant 메서드 추가 필요 (AlertRepository)
        // 여기서는 일단 findAll().stream().filter()로 구현 (추후 Repository 수정 권장)
        model.addAttribute("alerts", alertRepository.findAll().stream()
                .filter(a -> a.getTenant().equals(tenant))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .toList());
        return "alerts/list";
    }
}
