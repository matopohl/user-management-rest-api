package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);

    Optional<Role> findByNameAndIdNot(String name, UUID uuid);

}
