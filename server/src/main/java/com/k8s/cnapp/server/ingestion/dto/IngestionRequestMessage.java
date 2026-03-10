package com.k8s.cnapp.server.ingestion.dto;

import java.io.Serializable;

public record IngestionRequestMessage(
        Long tenantId,
        String rawData
) implements Serializable {}
