package com.k8s.cnapp.msa.auth.service;

/**
 * In-Port: 회원가입 유스케이스.
 * 테넌트 생성 + 기본 정책 생성 + ADMIN 사용자 생성 후 발급된 API Key 를 반환한다.
 */
public interface SignupUseCase {

    /**
     * @return 생성된 테넌트의 API Key
     * @throws com.k8s.cnapp.msa.auth.exception.DuplicateCompanyNameException 회사명이 이미 존재하는 경우
     * @throws com.k8s.cnapp.msa.auth.exception.DuplicateUsernameException 사용자명이 이미 존재하는 경우
     */
    String signup(String companyName, String username, String password);
}
