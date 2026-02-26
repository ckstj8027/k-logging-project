package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.NamespaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamespaceProfileRepository extends JpaRepository<NamespaceProfile, Long> {
    Optional<NamespaceProfile> findByName(String name);

    List<NamespaceProfile> findByNameIn(List<String> names);

    // 스냅샷에 없는 데이터 삭제 (동기화)
    @Modifying
    @Query("DELETE FROM NamespaceProfile n WHERE n.name NOT IN :names")
    void deleteByNameNotIn(@Param("names") List<String> names);
}
