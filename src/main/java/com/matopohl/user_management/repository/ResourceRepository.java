package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    Optional<Resource> findByRequestUrlAndRequestMethod(String url, String method);

    Optional<Resource> findByRequestUrlAndRequestMethodAndIdNot(String url, String method, UUID fromString);

    List<Resource> findAllByRequestUrlAndRequestMethodOrRequestUrlAndRequestMethod(String url, String method, String defaultUrl, String method1);

}
