package com.matopohl.user_management.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Profile("local")
@Configuration
public class EmbeddedRedisConfig {

    private final RedisServer redisServer;

    public EmbeddedRedisConfig(@Value("${spring.redis.port}") final int port) {
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void start() {
        this.redisServer.start();
    }

    @PreDestroy
    public void stop() {
        this.redisServer.stop();
    }

}
