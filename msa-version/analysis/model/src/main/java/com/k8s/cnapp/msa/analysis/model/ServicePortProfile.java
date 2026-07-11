package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

@Getter
public class ServicePortProfile {

    private ServiceProfile serviceProfile;
    private final String name;
    private final String protocol;
    private final Integer port;
    private final String targetPort;
    private final Integer nodePort;

    public ServicePortProfile(String name, String protocol, Integer port, String targetPort, Integer nodePort) {
        this.name = name;
        this.protocol = protocol;
        this.port = port;
        this.targetPort = targetPort;
        this.nodePort = nodePort;
    }

    public void setServiceProfile(ServiceProfile serviceProfile) {
        this.serviceProfile = serviceProfile;
    }
}
