package com.matopohl.user_management.repository;

import com.matopohl.user_management.domain.JWTTokenBlacklist;
import org.springframework.data.repository.Repository;

public interface JWTTokenBlacklistCacheRepository extends Repository<JWTTokenBlacklist, String> {

    JWTTokenBlacklist save(JWTTokenBlacklist entity);

    boolean existsByToken(String token);

}
