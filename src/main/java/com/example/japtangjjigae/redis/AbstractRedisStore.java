package com.example.japtangjjigae.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
public abstract class AbstractRedisStore {

    protected final StringRedisTemplate stringRedisTemplate;

    protected void setValue(String key, String value, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    protected Optional<String> getValue(String key) {
        String v = stringRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(v);
    }

    protected void deleteKey(String key) {
        stringRedisTemplate.delete(key);
    }

    protected void addSetMember(String key, String member) {
        stringRedisTemplate.opsForSet().add(key, member);
    }

    protected void removeSetMember(String key, String member) {
        stringRedisTemplate.opsForSet().remove(key, member);
    }

    protected Set<String> getSetMembers(String key) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);

        return members != null ? members : Collections.emptySet();
    }
}
