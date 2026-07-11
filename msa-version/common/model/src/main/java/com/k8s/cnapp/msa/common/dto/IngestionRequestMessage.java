package com.k8s.cnapp.msa.common.dto;

import java.io.Serializable;

public record IngestionRequestMessage(
        Long tenantId,
        String rawData // JSON string of ClusterSnapshot
) implements Serializable {}
