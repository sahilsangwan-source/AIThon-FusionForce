package com.lms.userservice.service;

import com.lms.userservice.constant.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for Redis cache operations
 */
@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set value in Redis with expiration time
     */
    public void set(String key, Object value, long timeoutSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
            log.debug("Redis set: {} with expiration: {} seconds", key, timeoutSeconds);
        } catch (Exception e) {
            log.error("Error setting Redis key: {}", key, e);
        }
    }

    /**
     * Get value from Redis
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting Redis key: {}", key, e);
            return null;
        }
    }

    /**
     * Delete key from Redis
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Redis delete: {}", key);
        } catch (Exception e) {
            log.error("Error deleting Redis key: {}", key, e);
        }
    }

    /**
     * Check if key exists in Redis
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking Redis key: {}", key, e);
            return false;
        }
    }

    /**
     * Cache user object
     */
    public void cacheUser(String userId, Object user, long timeoutSeconds) {
        set(AppConstants.REDIS_KEY_USER + userId, user, timeoutSeconds);
    }

    /**
     * Get cached user object
     */
    public Object getCachedUser(String userId) {
        return get(AppConstants.REDIS_KEY_USER + userId);
    }

    /**
     * Delete cached user
     */
    public void deleteCachedUser(String userId) {
        delete(AppConstants.REDIS_KEY_USER + userId);
    }

    /**
     * Cache session
     */
    public void cacheSession(String sessionId, Object session, long timeoutSeconds) {
        set(AppConstants.REDIS_KEY_SESSION + sessionId, session, timeoutSeconds);
    }

    /**
     * Get cached session
     */
    public Object getCachedSession(String sessionId) {
        return get(AppConstants.REDIS_KEY_SESSION + sessionId);
    }

    /**
     * Delete cached session
     */
    public void deleteCachedSession(String sessionId) {
        delete(AppConstants.REDIS_KEY_SESSION + sessionId);
    }
}
