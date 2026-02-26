package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.EventProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventProfileRepository extends JpaRepository<EventProfile, Long> {
    Optional<EventProfile> findByUid(String uid);

    List<EventProfile> findByUidIn(List<String> uids);

    // 스냅샷에 없는 데이터 삭제 (동기화)
    @Modifying
    @Query("DELETE FROM EventProfile e WHERE e.uid NOT IN :uids")
    void deleteByUidNotIn(@Param("uids") List<String> uids);
}
