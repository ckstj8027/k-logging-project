package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.NodeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeProfileRepository extends JpaRepository<NodeProfile, Long> {
    Optional<NodeProfile> findByName(String name);

    List<NodeProfile> findByNameIn(List<String> names);

    // 스냅샷에 없는 데이터 삭제 (동기화)
    @Modifying
    @Query("DELETE FROM NodeProfile n WHERE n.name NOT IN :names")
    void deleteByNameNotIn(@Param("names") List<String> names);
}
