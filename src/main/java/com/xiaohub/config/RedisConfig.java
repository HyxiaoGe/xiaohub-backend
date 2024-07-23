package com.xiaohub.config;

import com.xiaohub.properties.RedisProperties;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(RedisProperties.getIntProperty("redis.maxTotal"));
        poolConfig.setMaxIdle(RedisProperties.getIntProperty("redis.maxIdle"));
        poolConfig.setMinIdle(RedisProperties.getIntProperty("redis.minIdle"));
        poolConfig.setTestOnBorrow(RedisProperties.getBooleanProperty("redis.testOnBorrow"));
        poolConfig.setTestOnReturn(RedisProperties.getBooleanProperty("redis.testOnReturn"));
        poolConfig.setTestWhileIdle(RedisProperties.getBooleanProperty("redis.testWhileIdle"));

        String redisHost = RedisProperties.getProperty("redis.host");
        int redisPort = RedisProperties.getIntProperty("redis.port");
        int timeout = RedisProperties.getIntProperty("redis.timeout");

        jedisPool = new JedisPool(poolConfig, redisHost, redisPort, timeout);
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}


