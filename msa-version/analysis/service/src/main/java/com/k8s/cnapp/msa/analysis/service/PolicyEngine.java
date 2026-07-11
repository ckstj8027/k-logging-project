package com.k8s.cnapp.msa.analysis.service;

import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 정책 평가 엔진 (순수 자바 클래스).
 * 생성자에서 리소스 타입별로 정책을 그룹핑한다 (기존 @PostConstruct 대체).
 */
@Slf4j
public class PolicyEngine {

    private final Map<ResourceType, List<SecurityPolicy<?>>> policyGroupMap = new EnumMap<>(ResourceType.class);

    public PolicyEngine(List<SecurityPolicy<?>> allPolicies) {
        for (ResourceType type : ResourceType.values()) {
            List<SecurityPolicy<?>> policiesForType = allPolicies.stream()
                    .filter(policy -> policy.getSupportedType() == type)
                    .collect(Collectors.toList());

            policyGroupMap.put(type, policiesForType);
            log.info("Policy Engine: Grouped {} policies for AssetType: {}", policiesForType.size(), type);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PolicyEvaluationResult> evaluate(ResourceType resourceType, Object target, SecurityPolicyContext context) {
        List<SecurityPolicy<?>> applicablePolicies = policyGroupMap.getOrDefault(resourceType, Collections.emptyList());

        if (applicablePolicies.isEmpty()) {
            return Collections.emptyList();
        }

        List<PolicyEvaluationResult> results = new ArrayList<>();
        for (SecurityPolicy<?> policy : applicablePolicies) {
            try {
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
