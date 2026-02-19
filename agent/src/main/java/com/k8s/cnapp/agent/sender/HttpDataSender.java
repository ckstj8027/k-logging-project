package com.k8s.cnapp.agent.sender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.k8s.cnapp.agent.dto.ClusterSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "agent.sender.type", havingValue = "http", matchIfMissing = true)
public class HttpDataSender implements DataSender {

    private static final Logger logger = LoggerFactory.getLogger(HttpDataSender.class);

    private final Gson gson;
    private final RestTemplate restTemplate;

    @Value("${cnapp.server.url:http://localhost:8080/api/v1/ingestion/raw}")
    private String serverUrl;

    public HttpDataSender() {
        this.restTemplate = new RestTemplate();
        this.gson = new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
                    @Override
                    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        }
                    }

                    @Override
                    public OffsetDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return OffsetDateTime.parse(in.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                })
                .create();
    }

    @Override
    public void send(ClusterSnapshot snapshot) {
        String snapshotJson = gson.toJson(snapshot);
        logger.info("Sending snapshot via HTTP to {} (JSON size: {} bytes)", serverUrl, snapshotJson.length());
        logger.debug("Snapshot JSON: {}", snapshotJson);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(snapshotJson, headers);
            
            // 실제 전송
            restTemplate.postForObject(serverUrl, entity, String.class);
            
            logger.info("Successfully sent snapshot to server.");
        } catch (RestClientException e) {
            logger.error("Failed to send snapshot to server: {}", e.getMessage());
            throw e; // 상위 서비스에서 재시도 등을 처리할 수 있도록 예외 전파
        }
    }
}
