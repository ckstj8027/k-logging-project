package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.auth.infrastructure.policy.PolicyWriter;
import com.k8s.cnapp.msa.auth.model.Policy;
import com.k8s.cnapp.msa.auth.model.Tenant;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
class AuthPolicyService implements CreateDefaultPoliciesUseCase {

    private final PolicyReader policyReader;
    private final PolicyWriter policyWriter;

    @Override
    @Transactional
    public void createDefaultPoliciesForTenant(Tenant tenant) {
        // 1. Pod Policies
        createIfNotExists(tenant, ResourceType.POD, RuleType.PRIVILEGED_DENY, "true", "Disallow privileged containers");
        createIfNotExists(tenant, ResourceType.POD, RuleType.RUN_AS_ROOT_DENY, "true", "Disallow running as root");
        createIfNotExists(tenant, ResourceType.POD, RuleType.IMAGE_LATEST_TAG_DENY, "true", "Disallow 'latest' image tag");
        createIfNotExists(tenant, ResourceType.POD, RuleType.POD_DEFAULT_NAMESPACE_DENY, "true", "Disallow pods in 'default' namespace");

        // 2. Service Policies
        createIfNotExists(tenant, ResourceType.SERVICE, RuleType.PORT_BLACKLIST, "22,3306,5432", "Block sensitive infrastructure ports");
        createIfNotExists(tenant, ResourceType.SERVICE, RuleType.EXTERNAL_IP_DENY, "true", "Disallow LoadBalancer/NodePort without review");

        // 3. Deployment Policies
        createIfNotExists(tenant, ResourceType.DEPLOYMENT, RuleType.DEPLOYMENT_MIN_REPLICAS, "2", "Minimum 2 replicas for high availability");
        createIfNotExists(tenant, ResourceType.DEPLOYMENT, RuleType.DEPLOYMENT_MAX_REPLICAS, "10", "Prevent excessive resource consumption");
        createIfNotExists(tenant, ResourceType.DEPLOYMENT, RuleType.DEPLOYMENT_DEFAULT_NAMESPACE_DENY, "true", "Disallow deployments in 'default' namespace");

        // 4. Node Policies
        createIfNotExists(tenant, ResourceType.NODE, RuleType.NODE_CPU_LIMIT, "4", "Warn on nodes with > 4 CPUs for cost control");
        createIfNotExists(tenant, ResourceType.NODE, RuleType.NODE_MEMORY_LIMIT, "16Gi", "Warn on nodes with > 16Gi memory");
    }

    private void createIfNotExists(Tenant tenant, ResourceType resourceType, RuleType ruleType, String value, String description) {
        if (policyReader.findByTenantIdAndResourceTypeAndRuleType(tenant.id(), resourceType, ruleType).isEmpty()) {
            policyWriter.save(Policy.create(tenant.id(), resourceType, ruleType, value, description));
        }
    }
}
