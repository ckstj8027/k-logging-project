package com.k8s.cnapp.msa.common.model;

public enum RuleType {
    // Pod
    PRIVILEGED_DENY,
    RUN_AS_ROOT_DENY,
    IMAGE_LATEST_TAG_DENY,
    POD_DEFAULT_NAMESPACE_DENY,
    
    // Service
    PORT_BLACKLIST,
    EXTERNAL_IP_DENY,
    
    // Deployment
    DEPLOYMENT_MIN_REPLICAS,
    DEPLOYMENT_MAX_REPLICAS,
    DEPLOYMENT_DEFAULT_NAMESPACE_DENY,
    
    // Node
    NODE_CPU_LIMIT,
    NODE_MEMORY_LIMIT
}
