package com.k8s.cnapp.msa.query.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Collections;

/**
 * JWT 검증 전용 (발급은 auth 서비스 담당). 기존 config.JwtProvider 와 동일한 동작.
 */
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long tenantId = claims.get("tenantId", Long.class);
        String username = claims.getSubject();

        // CustomPrincipal을 사용하여 tenantId 저장
        CustomPrincipal principal = new CustomPrincipal(username, tenantId);

        return new UsernamePasswordAuthenticationToken(principal, token,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record CustomPrincipal(String username, Long tenantId) {}
}
