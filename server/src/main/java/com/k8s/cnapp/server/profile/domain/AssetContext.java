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

    @Column(name = "status")
    private String status;

    @Column(name = "pod_ip")
    private String podIp;

    @Column(name = "node_name")
    private String nodeName;

    // 생성자
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

    // DB 조회용 고유 키 (유니크 제약 조건과 일치: namespace + podName + containerName)
    public String getLookupKey() {
        return String.format("%s/%s/%s", namespace, podName, containerName);
    }

    // 자산 식별자 (UI 표시용: namespace/deploymentName 또는 namespace/podName)
    public String getAssetKey() {
        return String.format("%s/%s", namespace, deploymentName != null ? deploymentName : podName);
    }
}
