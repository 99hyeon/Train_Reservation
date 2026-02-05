package com.example.japtangjjigae.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        PolymorphicTypeValidator typeValidator =
            BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.japtangjjigae")
                .build();

        ObjectMapper objectMapper = new ObjectMapper()
//            .registerModule(new JavaTimeModule())
            .findAndRegisterModules()
            .activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        //기본 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .prefixCacheNameWith("cache:")
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
            .entryTtl(Duration.ofSeconds(30));

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("trainSearchBase",
            defaultConfig.entryTtl(Duration.ofSeconds(30)));
        configs.put("trainTotalSeats",
            defaultConfig.entryTtl(Duration.ofSeconds(6)));

        return RedisCacheManager.builder(cf)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configs)
            .build();
    }
}
