package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.ResetUserPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResetUserPasswordTokenRepository extends JpaRepository<ResetUserPasswordToken, UUID> {

}
