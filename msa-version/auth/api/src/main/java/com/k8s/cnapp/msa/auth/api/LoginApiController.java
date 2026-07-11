package com.k8s.cnapp.msa.auth.api;

import com.k8s.cnapp.msa.auth.model.User;
import com.k8s.cnapp.msa.auth.service.LoadUserUseCase;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginApiController {

    private final AuthenticationManager authenticationManager;
    private final AuthJwtService jwtService;
    private final LoadUserUseCase loadUserUseCase;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 도메인 User 에서 tenantId를 가져옵니다.
            User user = loadUserUseCase.findByUsername(userDetails.getUsername()).orElseThrow();
            String jwt = jwtService.generateToken(userDetails, user.tenantId());

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // [중요] 기존 서버 규격에 맞춰 에러 시 문자열을 반환합니다.
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
