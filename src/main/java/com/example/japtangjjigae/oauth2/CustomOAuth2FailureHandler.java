package com.example.japtangjjigae.oauth2;

import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.AuthResponseCode;
import com.example.japtangjjigae.global.response.code.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {

        ResponseCode responseCode = resolveResponseCode(exception);

        response.setStatus(responseCode.getCode());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> body = ApiResponse.from(responseCode);
        String json = objectMapper.writeValueAsString(body);

        response.getWriter().write(json);
    }

    private ResponseCode resolveResponseCode(AuthenticationException exception) {

        if(exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            OAuth2Error error = oAuth2Exception.getError();
            String errorCode = error.getErrorCode();

            try{
                return AuthResponseCode.valueOf(errorCode);
            }catch (IllegalArgumentException e){
                return AuthResponseCode.OAUTH2_AUTHENTICATION_FAILED;
            }
        }

        return AuthResponseCode.OAUTH2_AUTHENTICATION_FAILED;
    }
}
