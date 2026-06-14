package com.k8s.cnapp.msa.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetContext {

    @Column(name = "namespace", nullable = false)
    private String namespace;

    @Column(name = "pod_name", nullable = false)
    private String podName;

    @Column(name = "container_name", nullable = false)
    private String containerName;

    @Column(name = "image")
    private String image;

    @Column(name = "deployment_name")
    private String deploymentName;

    @Column(name = "status")
    private String status;

    @Column(name = "pod_ip")
    private String podIp;

    @Column(name = "node_name")
    private String nodeName;

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
