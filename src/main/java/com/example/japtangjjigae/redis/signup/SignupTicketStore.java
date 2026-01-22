package com.example.japtangjjigae.redis.signup;

import com.example.japtangjjigae.user.common.OAuthProvider;
import java.util.Optional;

public interface SignupTicketStore {
    void save(String ticket, SignupTicketValue value, long ttlSeconds);
    Optional<SignupTicketValue> get(String ticket);
    void invalidate(String ticket);

    record SignupTicketValue(String providerId, OAuthProvider provider){}
}
