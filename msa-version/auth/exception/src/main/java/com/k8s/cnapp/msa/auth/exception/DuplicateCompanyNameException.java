package com.k8s.cnapp.msa.auth.exception;

/** 회원가입 시 회사명이 이미 존재하는 경우. 메시지는 기존 응답 문자열과 동일해야 한다. */
public class DuplicateCompanyNameException extends RuntimeException {
    public DuplicateCompanyNameException() {
        super("Company name already exists");
    }
}
