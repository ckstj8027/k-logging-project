package com.k8s.cnapp.agent.sender;

import com.k8s.cnapp.agent.dto.ClusterSnapshot;

public interface DataSender {
    void send(ClusterSnapshot snapshot);
}
