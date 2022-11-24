package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> findAllByEmailOrUsername(String email, String username);

    List<User> findAllByEmailAndIdNotOrUsernameAndIdNot(String email, UUID uuid, String username, UUID uuid1);
}
