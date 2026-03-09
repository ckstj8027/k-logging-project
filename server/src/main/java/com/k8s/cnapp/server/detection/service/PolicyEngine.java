package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEngine {

    // 모든 SecurityPolicy 구현체 빈(Bean)들을 자동으로 주입받음
    private final List<SecurityPolicy<?>> allPolicies;

    // ResourceType별로 정책들을 미리 그룹핑하여 저장하는 Map (Policy Scope 최적화의 핵심)
    private final Map<Policy.ResourceType, List<SecurityPolicy<?>>> policyGroupMap = new EnumMap<>(Policy.ResourceType.class);

    /**
     * 애플리케이션 시작 시점에 정책들을 타입별로 미리 그룹핑 (Grouping logic)
     */
    @PostConstruct
    public void init() {
        for (Policy.ResourceType type : Policy.ResourceType.values()) {
            List<SecurityPolicy<?>> policiesForType = allPolicies.stream()
                    .filter(policy -> policy.getSupportedType() == type)
                    .collect(Collectors.toList());
            
            policyGroupMap.put(type, policiesForType);
            log.info("Policy Engine: Grouped {} policies for AssetType: {}", policiesForType.size(), type);
        }
    }

    /**
     * 특정 자산(Target)에 대해 그룹핑된 정책들만 실행하여 평가
     * (불필요한 Policy Evaluation을 원천적으로 차단)
     */
    @SuppressWarnings("unchecked")
    public List<PolicyEvaluationResult> evaluate(Policy.ResourceType resourceType, Object target, SecurityPolicyContext context) {
        // 1. 해당 타입의 정책 목록만 즉시 추출 (Map 조회)
        List<SecurityPolicy<?>> applicablePolicies = policyGroupMap.getOrDefault(resourceType, Collections.emptyList());

        if (applicablePolicies.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 해당 정책들만 평가
        List<PolicyEvaluationResult> results = new ArrayList<>();
        for (SecurityPolicy<?> policy : applicablePolicies) {
            try {
                // 타입 안정성을 보장하며 평가 실행
                SecurityPolicy<Object> p = (SecurityPolicy<Object>) policy;
                PolicyEvaluationResult result = p.evaluate(target, context);
                
                if (result.isViolated()) {
                    results.add(result);
                }
            } catch (Exception e) {
                log.error("Error evaluating policy: {}", policy.getClass().getSimpleName(), e);
            }
        }
        
        return results;
    }
}
