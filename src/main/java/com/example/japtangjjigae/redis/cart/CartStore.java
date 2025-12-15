package com.example.japtangjjigae.redis.cart;

import com.example.japtangjjigae.cart.entity.Cart;

public interface CartStore {

    Cart getOrCreate(Long userId);
    void save(Cart cart, long ttlSeconds);
    void clear(Long userId);

}
