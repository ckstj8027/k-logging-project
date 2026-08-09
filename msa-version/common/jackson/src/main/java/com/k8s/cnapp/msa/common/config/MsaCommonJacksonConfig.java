package com.k8s.cnapp.msa.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 기존 msa-common 의 전역 ObjectMapper 설정을 유지한다 (동작 보존).
 * IntOrString 직렬화가 필요한 analysis 는 consumer-kafka 모듈의
 * AnalysisJacksonAutoConfiguration 이 이 설정을 대체한다.
 */
@AutoConfiguration
public class MsaCommonJacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // 프론트의 new Date(문자열) 파싱을 위해 날짜를 배열이 아닌 ISO-8601 문자열로 직렬화한다
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
