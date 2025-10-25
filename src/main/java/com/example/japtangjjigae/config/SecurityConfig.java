package com.example.japtangjjigae.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((auth) -> auth
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                    // 카카오 로그인
                    .requestMatchers("/kakao/login", "/auth/login/kakao").permitAll()

                    .anyRequest().authenticated()
            );

        return http.build();
    }

}
