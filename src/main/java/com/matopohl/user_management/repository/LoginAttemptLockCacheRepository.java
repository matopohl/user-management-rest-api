package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.LoginAttemptLock;
import org.springframework.data.repository.Repository;

public interface LoginAttemptLockCacheRepository extends Repository<LoginAttemptLock, String> {

    LoginAttemptLock save(LoginAttemptLock entity);

    boolean existsByEmail(String email);

    LoginAttemptLock findByEmail(String email);

    Long getRemainingTTL(String id);

    Long deleteByEmail(String email);

}
