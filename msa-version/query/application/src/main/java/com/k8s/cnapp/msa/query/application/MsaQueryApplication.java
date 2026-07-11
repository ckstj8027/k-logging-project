package com.k8s.cnapp.msa.query.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 조립 전용 부트스트랩. componentScan 은 이 패키지만 스캔하며,
 * 실제 빈 배선은 각 모듈의 @AutoConfiguration(META-INF/spring/*.imports)이 담당한다.
 */
@SpringBootApplication
public class MsaQueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsaQueryApplication.class, args);
    }
}
