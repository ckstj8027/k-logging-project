package com.k8s.cnapp.msa.auth.api;

import com.k8s.cnapp.msa.auth.service.LoadUserUseCase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 기존 SecurityConfig 의 규칙(CORS/permitAll)을 그대로 보존한다.
 * Boot 기본 Security 자동설정보다 먼저 적용되어 기존과 동일하게 사용자 정의 체인이 우선한다.
 */
@AutoConfiguration(before = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableWebSecurity
@Import({LoginApiController.class, SignupApiController.class})
public class AuthApiAutoConfiguration {

    @Bean
    public AuthJwtService authJwtService() {
        return new AuthJwtService();
    }

    @Bean
    public UserDetailsService userDetailsService(LoadUserUseCase loadUserUseCase) {
        return new CustomUserDetailsService(loadUserUseCase);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 주의: mvcHandlerMappingIntrospector 도 CorsConfigurationSource 를 구현하므로
        // 타입 주입 대신 같은 클래스의 메서드를 직접 호출해 모호성을 제거한다.
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 허용
            .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/signup", "/api/login", "/api/auth/**").permitAll() // 인증 없이 접근 허용
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 모든 도메인 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
