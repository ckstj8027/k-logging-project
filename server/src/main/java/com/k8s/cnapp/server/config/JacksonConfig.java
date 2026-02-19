package com.k8s.cnapp.server.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.kubernetes.client.custom.IntOrString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(IntOrString.class, new IntOrStringSerializer());
        mapper.registerModule(module);
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
