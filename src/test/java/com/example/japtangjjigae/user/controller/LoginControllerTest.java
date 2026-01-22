package com.example.japtangjjigae.user.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.redis.signup.SignupTicketStore;
import com.example.japtangjjigae.redis.signup.SignupTicketStore.SignupTicketValue;
import com.example.japtangjjigae.user.common.OAuthProvider;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;

    @MockitoBean
    SignupTicketStore signupTicketStore;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        Mockito.reset(signupTicketStore);
    }

    @Test
    @DisplayName("signupTicket값이 없으면 TicketNotFoundException 처리")
    void signUp_ticketNotFound() throws Exception {
        //given
        String body = objectMapper.writeValueAsString(Map.of(
            "socialSignupTicket", "",
            "name", "홍길동",
            "phone", "010-1234-5678",
            "oauthProvider", OAuthProvider.KAKAO.toString()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(UserResponseCode.TICKET_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(UserResponseCode.TICKET_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("이미 회원가입을 했을 경우")
    void duplication_user() throws Exception {
        //given
        User user = User.createUser("123", OAuthProvider.KAKAO, "홍길동", "010-1234-5678");
        userRepository.save(user);

        Mockito.when(signupTicketStore.get("ticket-ok"))
            .thenReturn(Optional.of(new SignupTicketValue("kakao-999", OAuthProvider.KAKAO)));

        String body = objectMapper.writeValueAsString(Map.of(
            "socialSignupTicket", "ticket-ok",
            "name", "홍길동",
            "phone", "010-1234-5678",
            "oauthProvider", OAuthProvider.KAKAO.toString()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(UserResponseCode.USER_DUPLICATE.getCode()))
            .andExpect(jsonPath("$.message").value(UserResponseCode.USER_DUPLICATE.getMessage()));

    }

    @Test
    @DisplayName("다른 소셜 계정으로 이미 회원가입을 했을 경우")
    void duplication_user_diffrent_oAuth() throws Exception {
        //given
        User user = User.createUser("123", OAuthProvider.KAKAO, "홍길동", "010-1234-5678");
        userRepository.save(user);

        Mockito.when(signupTicketStore.get("ticket-ok"))
            .thenReturn(Optional.of(new SignupTicketValue("naver-999", OAuthProvider.NAVER)));

        String body = objectMapper.writeValueAsString(Map.of(
            "socialSignupTicket", "ticket-ok",
            "name", "홍길동",
            "phone", "010-1234-5678",
            "oauthProvider", OAuthProvider.NAVER.toString()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(UserResponseCode.ALREADY_LIKED_SOCIAL_ACCOUNT.getCode()))
            .andExpect(jsonPath("$.message").value(
                UserResponseCode.ALREADY_LIKED_SOCIAL_ACCOUNT.getMessage()));
    }


    @Test
    @DisplayName("회원가입 성공")
    void success_signup() throws Exception {
        //given
        Mockito.when(signupTicketStore.get("ticket-ok"))
            .thenReturn(Optional.of(new SignupTicketValue("naver-999", OAuthProvider.NAVER)));

        String body = objectMapper.writeValueAsString(Map.of(
            "socialSignupTicket", "ticket-ok",
            "name", "홍길동",
            "phone", "010-1234-5678",
            "oauthProvider", OAuthProvider.NAVER.toString()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data").exists());

        User saved = userRepository.findByNameAndPhone("홍길동", "010-1234-5678").orElse(null);
        assertEquals(OAuthProvider.NAVER, saved.getOauthProvider());
    }
}