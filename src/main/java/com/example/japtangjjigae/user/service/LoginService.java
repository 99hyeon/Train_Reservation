package com.example.japtangjjigae.user.service;

import com.example.japtangjjigae.exception.TicketNotFoundException;
import com.example.japtangjjigae.exception.UserDuplicateException;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.redis.signup.SignupTicketStore;
import com.example.japtangjjigae.redis.signup.SignupTicketStore.SignupTicketValue;
import com.example.japtangjjigae.user.dto.SignupRequestDTO;
import com.example.japtangjjigae.user.dto.SignupResponseDTO;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final SignupTicketStore signupTicketStore;

    public SignupResponseDTO signUp(SignupRequestDTO requestDto) {
        SignupTicketValue signupTicketValue = signupTicketStore.get(
            requestDto.getSocialSignupTicket()).orElseThrow(
            () -> new TicketNotFoundException(UserResponseCode.TICKET_NOT_FOUND)
        );

        User findUser = userRepository.findByNameAndPhone(requestDto.getName(), requestDto.getPhone()).orElse(null);

        if (findUser != null) {
            if(findUser.getOauthProvider() == signupTicketValue.provider()){
                throw new TicketNotFoundException(UserResponseCode.USER_DUPLICATE);
            }

            throw new UserDuplicateException(UserResponseCode.ALREADY_LIKED_SOCIAL_ACCOUNT);
        }

        User user = User.createUser(signupTicketValue.providerId(), signupTicketValue.provider(),
            requestDto.getName(), requestDto.getPhone());

        User savedUser = userRepository.save(user);
        signupTicketStore.invalidate(requestDto.getSocialSignupTicket());

        return new SignupResponseDTO(savedUser.getId(), requestDto.getOauthProvider());
    }

}
