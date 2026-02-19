package com.k8s.cnapp.agent.queue;

import com.k8s.cnapp.agent.dto.ClusterSnapshot;
import org.springframework.stereotype.Component;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class SnapshotBlockingQueue {

    // Set a capacity for the queue to prevent excessive memory usage.
    private final BlockingQueue<ClusterSnapshot> queue = new LinkedBlockingQueue<>(100);

    /**
     * Puts a cluster snapshot into the queue.
     * This method will block if the queue is full.
     * @param snapshot The ClusterSnapshot object.
     * @throws InterruptedException if interrupted while waiting.
     */
    public void put(ClusterSnapshot snapshot) throws InterruptedException {
        queue.put(snapshot);
    }

    /**
     * Takes a cluster snapshot from the queue.
     * This method will block if the queue is empty.
     * @return The ClusterSnapshot object.
     * @throws InterruptedException if interrupted while waiting.
     */
    public ClusterSnapshot take() throws InterruptedException {
        return queue.take();
    }
}
