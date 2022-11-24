package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Modifying
    @Query(
            value = "delete from refresh_tokens where refresh_token_id = :uuid",
            nativeQuery = true
    )
    @Override
    void deleteById(@Param("uuid") UUID uuid);

}
