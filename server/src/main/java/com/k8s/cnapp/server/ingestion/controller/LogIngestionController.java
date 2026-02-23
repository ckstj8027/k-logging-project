package com.k8s.cnapp.server.ingestion.controller;

import com.k8s.cnapp.server.ingestion.port.LogIngestionPort;
import com.k8s.cnapp.server.ingestion.service.LogProcessingService;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class LogIngestionController implements LogIngestionPort {

    private final LogProcessingService logProcessingService;

    @PostMapping("/raw")
    @Override
    public void ingest(@RequestBody String rawData) {
        // 1. 원본 로그 파싱 및 Profile 변환 위임
        logProcessingService.processRawData(rawData);
    }


}
