package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.RevInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RevInfoRepository extends JpaRepository<RevInfo, Integer> {

    Optional<RevInfo> findByRequestId(UUID id);

}
