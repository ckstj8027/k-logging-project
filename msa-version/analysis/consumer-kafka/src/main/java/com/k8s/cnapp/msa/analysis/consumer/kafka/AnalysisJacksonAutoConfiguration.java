package com.k8s.cnapp.msa.analysis.consumer.kafka;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kubernetes.client.custom.IntOrString;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * 기존 msa-common 의 MsaCommonJacksonConfig 를 그대로 옮긴 설정.
 * K8s SDK 의 IntOrString 직렬화 처리를 포함한다.
 */
@AutoConfiguration
public class AnalysisJacksonAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addSerializer(IntOrString.class, new IntOrStringSerializer());
        mapper.registerModule(module);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static class IntOrStringSerializer extends JsonSerializer<IntOrString> {
        @Override
        public void serialize(IntOrString value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value.isInteger()) {
                gen.writeNumber(value.getIntValue());
            } else {
                gen.writeString(value.getStrValue());
            }
        }
    }
}
