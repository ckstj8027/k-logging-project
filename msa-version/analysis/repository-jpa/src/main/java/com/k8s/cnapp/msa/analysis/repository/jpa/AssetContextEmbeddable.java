package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.AssetContext;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기존 도메인의 @Embeddable AssetContext 와 동일한 컬럼 매핑을 유지한다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetContextEmbeddable {

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

    static AssetContextEmbeddable fromModel(AssetContext model) {
        if (model == null) return null;
        AssetContextEmbeddable embeddable = new AssetContextEmbeddable();
        embeddable.namespace = model.getNamespace();
        embeddable.podName = model.getPodName();
        embeddable.containerName = model.getContainerName();
        embeddable.image = model.getImage();
        embeddable.deploymentName = model.getDeploymentName();
        embeddable.status = model.getStatus();
        embeddable.podIp = model.getPodIp();
        embeddable.nodeName = model.getNodeName();
        return embeddable;
    }

    AssetContext toModel() {
        return new AssetContext(namespace, podName, containerName, image, deploymentName, status, podIp, nodeName);
    }
}
