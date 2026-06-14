package com.k8s.cnapp.msa.analysis.service;

import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
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

    private final List<SecurityPolicy<?>> allPolicies;
    private final Map<ResourceType, List<SecurityPolicy<?>>> policyGroupMap = new EnumMap<>(ResourceType.class);

    @PostConstruct
    public void init() {
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
