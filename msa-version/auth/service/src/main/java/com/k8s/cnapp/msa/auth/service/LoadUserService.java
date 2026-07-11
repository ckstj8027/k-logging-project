package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.infrastructure.user.UserReader;
import com.k8s.cnapp.msa.auth.model.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class LoadUserService implements LoadUserUseCase {

    private final UserReader userReader;

    @Override
    public Optional<User> findByUsername(String username) {
        return userReader.findByUsername(username);
    }
}
