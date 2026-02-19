package com.k8s.cnapp.agent.sender;

import com.google.gson.Gson;
import com.k8s.cnapp.agent.dto.ClusterSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "agent.sender.type", havingValue = "kafka")
public class KafkaDataSender implements DataSender {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDataSender.class);
    private final Gson gson;

    // KafkaTemplate<String, String> kafkaTemplate; // 나중에 주입

    public KafkaDataSender(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void send(ClusterSnapshot snapshot) {
        String json = gson.toJson(snapshot);
        logger.info("Sending snapshot via Kafka (size: {} bytes)", json.length());

        // kafkaTemplate.send("cnapp-snapshot-topic", json);
        logger.warn("Kafka sender is not fully implemented yet.");
    }
}
