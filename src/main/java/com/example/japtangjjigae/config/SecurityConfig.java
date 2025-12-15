package com.example.japtangjjigae.config;


import com.example.japtangjjigae.jwt.JWTFilter;
import com.example.japtangjjigae.oauth2.CustomOAuth2FailureHandler;
import com.example.japtangjjigae.oauth2.CustomOAuth2UserService;
import com.example.japtangjjigae.oauth2.CustomOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 기본 보안 설정
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // JWT 필터
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정
        http
            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                .successHandler(customOAuth2SuccessHandler)
                .failureHandler(customOAuth2FailureHandler)
            );

        // 인가 정책
        http
            .authorizeHttpRequests((auth) -> auth
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                    .requestMatchers("/login/oauth2/code/naver", "/login/oauth2/code/kakao").permitAll()

                    .requestMatchers("/api/v1/check/signup", "/api/v1/users").authenticated()

                    .requestMatchers("/api/v1/trains", "/api/v1/seats", "/api/v1/cart").authenticated()

                    .anyRequest().authenticated()
//                    .anyRequest().permitAll()
            );

        return http.build();
    }

}
