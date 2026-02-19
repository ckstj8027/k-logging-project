package com.k8s.cnapp.server.profile.domain;

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
    private String deploymentName; // Optional

    // 생성자
    public AssetContext(String namespace, String podName, String containerName, String image, String deploymentName) {
        this.namespace = namespace;
        this.podName = podName;
        this.containerName = containerName;
        this.image = image;
        this.deploymentName = deploymentName;
    }

    // 자산 식별자 (예: namespace/deploymentName)
    public String getAssetKey() {
        return String.format("%s/%s", namespace, deploymentName != null ? deploymentName : podName);
    }
}
