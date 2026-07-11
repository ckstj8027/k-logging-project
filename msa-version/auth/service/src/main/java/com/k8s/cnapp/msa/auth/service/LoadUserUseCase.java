package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.model.User;

import java.util.Optional;

/**
 * In-Port: 사용자 조회 유스케이스. 로그인/인증 시 도메인 User 를 반환한다.
 */
public interface LoadUserUseCase {
    Optional<User> findByUsername(String username);
}
