package com.k8s.cnapp.server.view.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.domain.User;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.auth.repository.UserRepository;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SignupController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PolicyService policyService;

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String companyName,
                         @RequestParam String username,
                         @RequestParam String password,
                         Model model) {
        
        if (tenantRepository.findByName(companyName).isPresent()) {
            model.addAttribute("error", "Company name already exists");
            return "signup";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }

        // 1. Tenant 생성
        Tenant tenant = new Tenant(companyName);
        tenantRepository.save(tenant);

        // 2. 기본 정책 생성
        policyService.createDefaultPoliciesForTenant(tenant);

        // 3. Admin User 생성
        // 패스워드 인코딩은 SecurityConfig 설정에 따라 적용 필요 (현재는 {noop} 접두어 사용)
        User user = new User(username, "{noop}" + password, User.Role.ADMIN, tenant);
        userRepository.save(user);

        // 4. 가입 완료 페이지로 이동 (API Key 보여주기 위해)
        model.addAttribute("apiKey", tenant.getApiKey());
        return "signup_success";
    }
}
