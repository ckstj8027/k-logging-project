package com.k8s.cnapp.msa.query.api;

import com.k8s.cnapp.msa.query.model.Policy;
import com.k8s.cnapp.msa.query.service.PolicyUpdateUseCase;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 기존 PolicyCommandController 와 동일한 URL/요청/응답 형태. 이제 UseCase 만 호출한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyCommandController {

    private final PolicyUpdateUseCase policyUpdateUseCase;

    private Long getCurrentTenantId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtProvider.CustomPrincipal) {
            return ((JwtProvider.CustomPrincipal) principal).tenantId();
        }
        return 1L; // Fallback
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable Long id, @RequestBody PolicyUpdateRequest request) {
        Long tenantId = getCurrentTenantId();
        log.info("Policy update request for tenant: {}, policy: {}", tenantId, id);

        Optional<Policy> updated = policyUpdateUseCase.update(tenantId, id, request.getValue(), request.isEnabled());
        if (updated.isEmpty()) {
            return ResponseEntity.status(403).body("Access denied or policy not found");
        }

        return ResponseEntity.ok(updated.get());
    }

    @Data
    public static class PolicyUpdateRequest {
        private String value;
        private boolean enabled;
    }
}
