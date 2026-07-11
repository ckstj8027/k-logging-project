package com.k8s.cnapp.msa.ingestion.exception;

public class InvalidApiKeyException extends RuntimeException {
    public InvalidApiKeyException(String apiKey) {
        super("Invalid API Key: " + apiKey);
    }
}
