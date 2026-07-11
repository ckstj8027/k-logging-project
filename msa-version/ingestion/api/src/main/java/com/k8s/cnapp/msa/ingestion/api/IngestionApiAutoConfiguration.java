package com.k8s.cnapp.msa.ingestion.api;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(IngestionController.class)
public class IngestionApiAutoConfiguration {
}
