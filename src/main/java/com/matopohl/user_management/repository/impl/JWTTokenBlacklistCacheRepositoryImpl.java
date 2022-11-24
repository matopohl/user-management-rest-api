package com.matopohl.user_management.repository.impl;

import com.matopohl.user_management.domain.JWTTokenBlacklist;
import com.matopohl.user_management.repository.JWTTokenBlacklistCacheRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Primary
@Component
public class JWTTokenBlacklistCacheRepositoryImpl extends CacheRepository<JWTTokenBlacklist> implements JWTTokenBlacklistCacheRepository {

    public JWTTokenBlacklistCacheRepositoryImpl(Jedis jedis) {
        super(jedis);
    }

    @Override
    public JWTTokenBlacklist save(JWTTokenBlacklist entity) {
        return super.save(entity);
    }

    @Override
    public boolean existsByToken(String token) {
        return super.exists(token);
    }

}
