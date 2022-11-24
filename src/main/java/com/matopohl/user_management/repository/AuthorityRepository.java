package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {

    Optional<Authority> findById(UUID id);

    Optional<Authority> findByName(String name);

    Optional<Authority> findByNameAndIdNot(String name, UUID fromString);

}
