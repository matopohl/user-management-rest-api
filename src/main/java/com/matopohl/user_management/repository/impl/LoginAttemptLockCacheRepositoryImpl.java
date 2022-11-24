package com.matopohl.user_management.repository.impl;

import com.matopohl.user_management.domain.LoginAttemptLock;
import com.matopohl.user_management.repository.LoginAttemptLockCacheRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Primary
@Component
public class LoginAttemptLockCacheRepositoryImpl extends CacheRepository<LoginAttemptLock> implements LoginAttemptLockCacheRepository {

    public LoginAttemptLockCacheRepositoryImpl(Jedis jedis) {
        super(jedis);
    }

    @Override
    public LoginAttemptLock save(LoginAttemptLock entity) {
        return super.save(entity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return super.exists(email);
    }

    @Override
    public LoginAttemptLock findByEmail(String email) {
        return super.findById(email);
    }

    @Override
    public Long getRemainingTTL(String id) {
        return super.getRemainingTTL(id);
    }

    @Override
    public Long deleteByEmail(String email) {
        return super.delete(email);
    }

}
