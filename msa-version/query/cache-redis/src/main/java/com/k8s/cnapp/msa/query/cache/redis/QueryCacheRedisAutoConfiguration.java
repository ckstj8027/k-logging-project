package com.k8s.cnapp.msa.query.cache.redis;

import com.k8s.cnapp.msa.query.infrastructure.alert.AlertCachePort;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
public class QueryCacheRedisAutoConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // JSON 직렬화를 통해 캐시 가독성 및 호환성 확보 (기존 RedisConfig 와 동일)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public AlertCachePort alertCachePort(RedisTemplate<String, Object> redisTemplate) {
        return new AlertRedisCacheAdapter(redisTemplate);
    }
}
