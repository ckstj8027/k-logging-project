package com.k8s.cnapp.msa.auth.repository.jpa;

import com.k8s.cnapp.msa.auth.infrastructure.user.UserReader;
import com.k8s.cnapp.msa.auth.infrastructure.user.UserWriter;
import com.k8s.cnapp.msa.auth.model.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class UserJpaAdapter implements UserReader, UserWriter {

    private final UserJpaRepository userJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(UserEntity::toModel);
    }

    @Override
    public void save(User user) {
        TenantEntity tenant = tenantJpaRepository.getReferenceById(user.tenantId());
        userJpaRepository.save(new UserEntity(user.username(), user.password(), user.role(), tenant));
    }
}
