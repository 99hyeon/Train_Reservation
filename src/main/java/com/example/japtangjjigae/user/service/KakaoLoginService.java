package com.example.japtangjjigae.user.service;

import com.example.japtangjjigae.exception.handler.UserDuplicateException;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.redis.SignupTicketStore;
import com.example.japtangjjigae.redis.SignupTicketStore.SignupTicketValue;
import com.example.japtangjjigae.user.dto.KakaoSignupRequestDTO;
import com.example.japtangjjigae.user.dto.KakaoSignupResponseDTO;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final UserRepository userRepository;
    private final SignupTicketStore signupTicketStore;

    public KakaoSignupResponseDTO signUp(KakaoSignupRequestDTO requestDto) {
        SignupTicketValue signupTicketValue = signupTicketStore.get(
            requestDto.getSocialSignupTicket()).orElse(null);
        signupTicketStore.invalidate(requestDto.getSocialSignupTicket());

        User findUser = userRepository.findBySocialIdAndOAuthProvider(signupTicketValue.kakaoId(),
            signupTicketValue.provider()).orElse(null);

        if (findUser != null) {
            throw new UserDuplicateException(UserResponseCode.USER_DUPLICATE);
        }

        User user = User.createUser(signupTicketValue.kakaoId(), signupTicketValue.provider(),
            requestDto.getName(), requestDto.getPhone());

        User savedUser = userRepository.save(user);

        return new KakaoSignupResponseDTO(savedUser.getId());
    }

}
