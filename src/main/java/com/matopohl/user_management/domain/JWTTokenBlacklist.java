package com.matopohl.user_management.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Accessors(chain = true)
@RedisHash(value = "JWTTokenBlacklist")
public class JWTTokenBlacklist {

    @Id
    private String token;

    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long ttl;

    private UUID userId;

}