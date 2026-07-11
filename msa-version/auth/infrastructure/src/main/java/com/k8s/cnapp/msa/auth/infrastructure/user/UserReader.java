package com.k8s.cnapp.msa.auth.infrastructure.user;

import com.k8s.cnapp.msa.auth.model.User;

import java.util.Optional;

/**
 * Out-Port: 사용자 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface UserReader {
    Optional<User> findByUsername(String username);
}
