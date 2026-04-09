package com.k8s.cnapp.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "scanExecutor")
    public Executor scanExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // CPU가 2개인 환경이므로, I/O 대기 시간을 고려하여 코어 4개, 최대 8개로 설정
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        // 대량 로그 유입 시 메모리 보호를 위해 큐 크기 제한
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Scanner-");
        
        // 큐가 가득 찼을 때, 호출한 스레드(MQ 리스너)가 직접 실행하게 하여 유량 조절(Backpressure)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
}
