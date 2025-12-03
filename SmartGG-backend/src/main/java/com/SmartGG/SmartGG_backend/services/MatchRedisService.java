package com.SmartGG.SmartGG_backend.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class MatchRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public MatchRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveUserMatch(String puuid, String matchId, Map<String, Object> matchData) {
        String redisKey = "userMatches:" + puuid; 

        if (!Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(redisKey, matchId))) {
            redisTemplate.opsForHash().put(redisKey, matchId, matchData);
        }
    }

    public Map<Object, Object> getUserMatches(String puuid) {
        String redisKey = "userMatches:" + puuid;
        return redisTemplate.opsForHash().entries(redisKey);
    }
}
