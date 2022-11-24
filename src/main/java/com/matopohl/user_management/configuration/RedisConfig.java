package com.matopohl.user_management.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import static org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents.ON_DEMAND;
import static org.springframework.data.redis.core.RedisKeyValueAdapter.ShadowCopy.OFF;

@EnableRedisRepositories(enableKeyspaceEvents = ON_DEMAND, keyspaceNotificationsConfigParameter = "", shadowCopy = OFF)
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.post:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.ssl:false}")
    private boolean ssl;

    @DependsOn("embeddedRedisConfig")
    @Profile("local")
    @Bean(name = "jedis")
    public Jedis jedisLocal() {
        return new Jedis(host, port);
    }

    @Profile("!local")
    @Bean(name = "jedis")
    public Jedis jedis() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        try(JedisPool jedisPool = new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, (password.equals("") ? null : password), ssl)) {
            return jedisPool.getResource();
        }
    }

}
