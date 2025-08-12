package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisServiceImpl<T> implements RedisService<T> {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;


    @SuppressWarnings("unchecked")
    @Override
    public T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        try {
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.convertValue(value, clazz);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to convert cached value to " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public void set(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean isMemberOfSet(String key, Object member) {
        Boolean result = redisTemplate.opsForSet().isMember(key, member);
        return result != null && result;
    }

    @Override
    public void addToSet(String key, Object member) {
        redisTemplate.opsForSet().add(key, member);
    }

    @Override
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }
}
