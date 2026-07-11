package com.k8s.cnapp.msa.auth.infrastructure.user;

import com.k8s.cnapp.msa.auth.model.User;

/**
 * Out-Port: 사용자 저장 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface UserWriter {
    void save(User user);
}
