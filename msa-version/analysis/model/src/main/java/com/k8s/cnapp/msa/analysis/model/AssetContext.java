package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

/**
 * Pod 자산 식별/상태 정보 (순수 POJO).
 */
@Getter
public class AssetContext {

    private final String namespace;
    private final String podName;
    private final String containerName;
    private final String image;
    private final String deploymentName;
    private final String status;
    private final String podIp;
    private final String nodeName;

    public AssetContext(String namespace, String podName, String containerName, String image, String deploymentName, String status, String podIp, String nodeName) {
        this.namespace = namespace;
        this.podName = podName;
        this.containerName = containerName;
        this.image = image;
        this.deploymentName = deploymentName;
        this.status = status;
        this.podIp = podIp;
        this.nodeName = nodeName;
    }

    public String getLookupKey() {
        return String.format("%s/%s/%s", namespace, podName, containerName);
    }

    public String getAssetKey() {
        return String.format("%s/%s", namespace, deploymentName != null ? deploymentName : podName);
    }
}
