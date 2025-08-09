//package com.dbserver.votingchallenge.config;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@Configuration
//@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
//public class RedisConfig {
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        return new LettuceConnectionFactory();
//    }
//
//    @Bean
//    public RedisTemplate<String, Long> longRedisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Long> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        return template;
//    }
//}
