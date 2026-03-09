package com.k8s.cnapp.server.profile.dto;

import com.k8s.cnapp.server.profile.domain.ServicePortProfile;
import lombok.Getter;

@Getter
public class ServicePortProfileDto {
    private String name;
    private String protocol;
    private Integer port;
    private String targetPort; // Integer -> String 변경
    private Integer nodePort;

    public ServicePortProfileDto(ServicePortProfile portProfile) {
        this.name = portProfile.getName();
        this.protocol = portProfile.getProtocol();
        this.port = portProfile.getPort();
        this.targetPort = portProfile.getTargetPort();
        this.nodePort = portProfile.getNodePort();
    }
}
