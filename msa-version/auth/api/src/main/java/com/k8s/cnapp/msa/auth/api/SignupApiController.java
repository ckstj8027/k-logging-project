package com.k8s.cnapp.msa.auth.api;

import com.k8s.cnapp.msa.auth.exception.DuplicateCompanyNameException;
import com.k8s.cnapp.msa.auth.exception.DuplicateUsernameException;
import com.k8s.cnapp.msa.auth.service.SignupUseCase;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SignupApiController {

    private final SignupUseCase signupUseCase;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        log.info("Received signup request for user: {}", request.getUsername());

        try {
            String apiKey = signupUseCase.signup(request.getCompanyName(), request.getUsername(), request.getPassword());

            Map<String, String> response = new HashMap<>();
            response.put("apiKey", apiKey);
            return ResponseEntity.ok(response);

        } catch (DuplicateCompanyNameException | DuplicateUsernameException e) {
            // [중요] 기존 서버 규격에 맞춰 에러 시 JSON이 아닌 문자열(String)을 반환합니다.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class SignupRequest {
        private String companyName;
        private String username;
        private String password;
    }
}
