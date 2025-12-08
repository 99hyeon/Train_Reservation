package com.example.japtangjjigae.redis;

import com.example.japtangjjigae.user.common.OAuthProvider;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSignupTicketStore extends AbstractRedisStore implements SignupTicketStore {

    private static final String KEY_PREFIX = "signup:ticket:";

    public RedisSignupTicketStore(StringRedisTemplate stringRedisTemplate){
        super(stringRedisTemplate);
    }

    @Override
    public void save(String ticket, SignupTicketValue value, long ttlSeconds) {
        String key = KEY_PREFIX + ticket;
        String payload = value.providerId() + ":" + value.provider();
        setValue(key, payload, ttlSeconds);
    }

    @Override
    public Optional<SignupTicketValue> get(String ticket) {
        String key = KEY_PREFIX + ticket;

        return getValue(key).map(
            s -> {
                String[] p = s.split(":", 2);
                return new SignupTicketValue(p[0], OAuthProvider.valueOf(p[1]));
            }
        );
    }

    @Override
    public void invalidate(String ticket) {
        deleteKey(KEY_PREFIX + ticket);
    }
}
