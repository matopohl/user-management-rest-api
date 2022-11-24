package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogRepository extends JpaRepository<Log, UUID> {

}
