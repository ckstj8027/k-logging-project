package com.k8s.cnapp.msa.analysis.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 조립 전용 부트스트랩. componentScan 은 이 패키지만 스캔하며,
 * 실제 빈 배선은 각 모듈의 @AutoConfiguration(META-INF/spring/*.imports)이 담당한다.
 */
@Slf4j
@SpringBootApplication
public class MsaAnalysisApplication {
    public static void main(String[] args) {
        log.info("=================================================");
        log.info("K-SENTRY ANALYSIS ENGINE STARTING - v2026.06.15.1");
        log.info("=================================================");
        SpringApplication.run(MsaAnalysisApplication.class, args);
    }
}
