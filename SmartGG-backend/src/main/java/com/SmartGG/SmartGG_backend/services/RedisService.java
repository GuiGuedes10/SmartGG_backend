package com.SmartGG.SmartGG_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void putHash(String hashName, String key, Object value) {
        redisTemplate.opsForHash().put(hashName, key, value);
    }

    public Object getHash(String hashName, String key) {
        return redisTemplate.opsForHash().get(hashName, key);
    }

    public void deleteHash(String hashName, String key) {
        redisTemplate.opsForHash().delete(hashName, key);
    }
}
