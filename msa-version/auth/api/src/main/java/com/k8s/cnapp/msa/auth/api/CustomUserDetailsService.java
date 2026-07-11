package com.k8s.cnapp.msa.auth.api;

import com.k8s.cnapp.msa.auth.model.User;
import com.k8s.cnapp.msa.auth.service.LoadUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

@RequiredArgsConstructor
class CustomUserDetailsService implements UserDetailsService {

    private final LoadUserUseCase loadUserUseCase;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = loadUserUseCase.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.username(),
                user.password(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
        );
    }
}
