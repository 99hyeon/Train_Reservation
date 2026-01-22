package com.example.japtangjjigae.user.repository;

import com.example.japtangjjigae.user.common.OAuthProvider;
import com.example.japtangjjigae.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialIdAndOauthProvider(String socialId, OAuthProvider oauthProvider);
    Optional<User> findByNameAndPhone(String name, String phone);

}
