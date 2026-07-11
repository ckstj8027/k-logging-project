package com.k8s.cnapp.msa.query.api;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        AssetQueryController.class,
        PolicyCommandController.class,
        JwtProvider.class,
        JwtAuthFilter.class,
        SecurityConfig.class
})
public class QueryApiAutoConfiguration {
}
