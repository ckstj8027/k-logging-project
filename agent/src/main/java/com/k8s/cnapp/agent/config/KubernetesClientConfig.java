package com.k8s.cnapp.agent.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class KubernetesClientConfig {

    @Bean
    public ApiClient kubernetesApiClient() throws IOException {
        // This will attempt to load the in-cluster configuration first.
        // If that fails (e.g., not running inside a pod), it will fall back to
        // the default kubeconfig file (~/.kube/config).
        ApiClient client = ClientBuilder.standard().build();
        Configuration.setDefaultApiClient(client);
        return client;
    }
}
