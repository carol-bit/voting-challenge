package com.dbserver.votingchallenge.service;

import java.util.concurrent.TimeUnit;

public interface RedisService<T> {

    T get(String key, Class<T> clazz);

    void set(String key, T value, long timeout, TimeUnit unit);

    void delete(String key);

    boolean isMemberOfSet(String key, Object member);

    void addToSet(String key, Object member);

    void expire(String key, long timeout, TimeUnit unit);
}
