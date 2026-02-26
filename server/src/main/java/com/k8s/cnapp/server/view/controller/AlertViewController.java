package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertViewController {

    private final AlertRepository alertRepository;

    @GetMapping
    public String alerts(Model model) {
        model.addAttribute("alerts", alertRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "alerts/list";
    }
}
