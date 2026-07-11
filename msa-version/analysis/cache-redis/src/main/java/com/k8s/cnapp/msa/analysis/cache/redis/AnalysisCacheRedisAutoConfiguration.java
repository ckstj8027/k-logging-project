package com.k8s.cnapp.msa.analysis.cache.redis;

import com.k8s.cnapp.msa.analysis.infrastructure.cache.AlertCacheInvalidationPort;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
public class AnalysisCacheRedisAutoConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // msa-query와 동일한 JSON 직렬화 방식을 사용하여 데이터 호환성 유지
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public AlertCacheInvalidationPort alertCacheInvalidationPort(RedisTemplate<String, Object> redisTemplate) {
        return new RedisAlertCacheInvalidationAdapter(redisTemplate);
    }
}
