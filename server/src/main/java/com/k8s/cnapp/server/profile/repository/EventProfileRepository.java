package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.EventProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventProfileRepository extends JpaRepository<EventProfile, Long> {
    Optional<EventProfile> findByUid(String uid);

    List<EventProfile> findByUidIn(List<String> uids);
}
