package com.k8s.cnapp.agent.service;

import com.k8s.cnapp.agent.dto.ClusterSnapshot;
import com.k8s.cnapp.agent.queue.SnapshotBlockingQueue;
import com.k8s.cnapp.agent.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class DataForwarderService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataForwarderService.class);

    private final SnapshotBlockingQueue queue;
    private final DataSender dataSender;

    public DataForwarderService(SnapshotBlockingQueue queue, DataSender dataSender) {
        this.queue = queue;
        this.dataSender = dataSender;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting data forwarder service...");

        // Run the forwarding logic in a separate thread
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ClusterSnapshot snapshot = queue.take();
                    forwardData(snapshot);
                } catch (InterruptedException e) {
                    logger.info("Data forwarder thread interrupted. Exiting.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Failed to forward data. Will retry.", e);
                }
            }
        }).start();
    }

    private void forwardData(ClusterSnapshot snapshot) {
        try {
            dataSender.send(snapshot);
        } catch (Exception e) {
            logger.error("Error during data sending: {}", e.getMessage());
            // 재시도 로직이나 큐에 다시 넣는 로직을 여기에 추가할 수 있습니다.
            // 예: queue.put(snapshot); (주의: 무한 루프 가능성)
        }
    }
}
