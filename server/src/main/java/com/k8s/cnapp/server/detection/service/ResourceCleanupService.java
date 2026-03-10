package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceCleanupService {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final EventProfileRepository eventProfileRepository;

    /**
     * 1분마다 실행 (Garbage Collection)
     * 최근 2분 동안 에이전트로부터 생존 보고(lastSeenAt)가 없는 리소스를 삭제합니다.
     */
    @Scheduled(fixedRate = 60000) // 1분 (60,000ms)
    @SchedulerLock(name = "ResourceCleanupService_cleanupOldResources", lockAtMostFor = "50s", lockAtLeastFor = "10s")
    @Transactional
    public void cleanupOldResources() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        log.info("Starting Extreme Resource Cleanup (GC) for resources not seen since {}", threshold);

        try {
            podProfileRepository.deleteAllByLastSeenAtBefore(threshold);
            serviceProfileRepository.deleteAllByLastSeenAtBefore(threshold);
            nodeProfileRepository.deleteAllByLastSeenAtBefore(threshold);
            deploymentProfileRepository.deleteAllByLastSeenAtBefore(threshold);
            namespaceProfileRepository.deleteAllByLastSeenAtBefore(threshold);
            
            // 이벤트는 데이터 양이 많을 수 있으므로 함께 정리
            eventProfileRepository.deleteAllByLastSeenAtBefore(threshold);

            log.info("Resource Cleanup completed successfully.");
        } catch (Exception e) {
            log.error("Error during resource cleanup", e);
        }
    }
}
