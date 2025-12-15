package com.example.japtangjjigae.redis.pay;

import java.util.Optional;

public interface PayStore {

    void save(Long orderId, String tid, long ttlSeconds);

    Optional<String> getTid(Long orderId);

    void delete(Long orderId);

}
