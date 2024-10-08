package com.xiaohub.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaohub.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;

public class RedisUtil {

    private static JedisPool jedisPool = RedisConfig.getJedisPool();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // Key（键），简单的key-value操作

    public static boolean keyIsExist(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public static long ttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }

    public static void expire(String key, long timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, timeout);
        }
    }

    public static long increment(String key, long delta) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, delta);
        }
    }

    public static long incrementHash(String name, String key, long delta) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrBy(name, key, delta);
        }
    }

    public static long decrementHash(String name, String key, long delta) {
        return incrementHash(name, key, -delta);
    }

    public static void setHashValue(String name, String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(name, key, value);
        }
    }

    public static String getHashValue(String name, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(name, key);
        }
    }

    public static Map<String, String> getHashAll(String name) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(name);
        }
    }


    public static long decrement(String key, long delta) {
        return increment(key, -delta);
    }

    public static Set<String> keys(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }
    }

    public static void del(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public static void allDel(String key) {
        Set<String> keys = keys(key + "*");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(keys.toArray(new String[0]));
        }
    }

    // String（字符串）

    public static void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public static void set(String key, String value, long timeout) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, timeout, value);
        }
    }

    public static void setnx60s(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setnx(key, value);
            jedis.expire(key, 60);
        }
    }

    public static Boolean setnx(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setnx(key, value) == 1;
        }
    }

    public static Boolean setnx(String key, String value, Integer seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            boolean result = jedis.setnx(key, value) == 1;
            if (result) {
                jedis.expire(key, seconds);
            }
            return result;
        }
    }

    public static String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public static List<String> mget(List<String> keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.mget(keys.toArray(new String[0]));
        }
    }

    // Hash（哈希表）

    public static void hset(String key, String field, Object value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value.toString());
        }
    }

    public static String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }


    // List（列表）

    public static <T> void saveList(String key, List<T> list) {
        try (Jedis jedis = jedisPool.getResource()) {
            for (T item : list) {
                String itemJson = objectMapper.writeValueAsString(item);
                jedis.rpush(key, itemJson);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> getList(String key, Class<T> clazz, int start, int stop) {
        List<T> list = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> items = jedis.lrange(key, start, stop);
            for (String itemJson : items) {
                T item = objectMapper.readValue(itemJson, clazz);
                list.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 从Redis获取最新的数据项：从Redis中获取列表的第一条数据，这是最近一次添加的数据。
     * 确定新数据：比较API返回的数据列表和从Redis获取的最新数据，找出新的数据项。
     * 存储新数据：将确认为新的数据项使用LPUSH命令推送到Redis列表的前端。
     * @param key
     * @param currentItems
     * @param clazz
     * @param <T>
     */
    public static <T> boolean saveList(String key, List<T> currentItems, Class<T> clazz) {
        boolean hasItem = false;
        try (Jedis jedis = jedisPool.getResource()) {
            //  从redis获取截至目前最新的数据
            String latestItemJson = jedis.lindex(key, 0);
            T latestItem = null;
            if (latestItemJson != null) {
                latestItem = objectMapper.readValue(latestItemJson, clazz);
            }

            List<T> newItems = new ArrayList<>();
            for (T item : currentItems) {
                if (item.equals(latestItem)) {
                    break;  //  当与之相匹配时，证明当前及之后的数据都是旧数据
                }
                newItems.add(item);
            }
            if (!newItems.isEmpty()) {
                hasItem = true;
            }
            Collections.reverse(newItems);
            for (T item : newItems) {
                String itemJson = objectMapper.writeValueAsString(item);
                jedis.lpush(key, itemJson);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return hasItem;
    }

    public static long lpush(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpush(key, value);
        }
    }

    public static String lpop(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpop(key);
        }
    }

    public static long rpush(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpush(key, value);
        }
    }

    // 运行Lua脚本

    public static Long execLuaScript(String script, String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return (Long) jedis.eval(script, 1, key, value);
        }
    }
}

