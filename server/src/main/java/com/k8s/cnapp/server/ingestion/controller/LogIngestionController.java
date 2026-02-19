package com.k8s.cnapp.server.ingestion.controller;

import com.k8s.cnapp.server.ingestion.port.LogIngestionPort;
import com.k8s.cnapp.server.profile.domain.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class LogIngestionController implements LogIngestionPort {

    // 실제 구현체(Service) 주입 필요
    // private final LogProcessingService logProcessingService;

    @PostMapping("/raw")
    @Override
    public void ingest(@RequestBody String rawData) {
        // 1. 원본 로그 파싱
        // 2. Profile 객체로 변환
        // 3. BaselineService로 전달
        System.out.println("Received raw data: " + rawData);
    }

    @PostMapping("/profile")
    @Override
    public void ingest(@RequestBody Profile profile) {
        // 1. Profile 객체 수신 (Agent가 이미 가공한 경우)
        // 2. BaselineService로 전달
        System.out.println("Received profile: " + profile.getAssetContext().getAssetKey());
    }
}
