package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.VerifyUserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerifyUserTokenRepository extends JpaRepository<VerifyUserToken, UUID> {

}
