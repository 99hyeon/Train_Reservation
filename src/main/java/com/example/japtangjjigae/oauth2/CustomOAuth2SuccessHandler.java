package com.example.japtangjjigae.oauth2;

import com.example.japtangjjigae.jwt.JWTUtil;
import com.example.japtangjjigae.user.common.OAuthProvider;
import com.example.japtangjjigae.user.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /**
     * 배포할 시 URL 변경 필요
     * 임시로 해둔 것
     */
    //todo: 해당 url 만들어야 함
    private static final String SIGNUP_REDIRECT_URL = "http://localhost:8080/api/v1/check/signup?ticket=";
    private static final String HOME_REDIRECT_URL = "http://localhost:8080/";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "Authorization";

    private static final long ACCESS_TOKEN_TTL_SECONDS = 60*60L;
    private static final int ACCESS_TOKEN_COOKIE_MAX_AGE = (int) ACCESS_TOKEN_TTL_SECONDS;


    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        if(!(authentication.getPrincipal() instanceof CustomOAuth2User customUserDetails)){
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        String signupTicket = customUserDetails.getSignupTicket();
        //회원가입 대상 -> 회원가입 페이지로 리다이렉트
        if(signupTicket != null){
            String encodedTicket = URLEncoder.encode(signupTicket, StandardCharsets.UTF_8);
            String redirectUrl = SIGNUP_REDIRECT_URL + encodedTicket;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            return;
        }

        UserDTO userDto = customUserDetails.getUserDto();
        String name = customUserDetails.getName();
        Long userId = userDto.getId();
        OAuthProvider oAuthProvider = userDto.getOAuthProvider();

        String token = jwtUtil.createJwt(userId, name, oAuthProvider, ACCESS_TOKEN_TTL_SECONDS);    // 1시간

        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE_NAME, token));
        response.sendRedirect(HOME_REDIRECT_URL);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(ACCESS_TOKEN_COOKIE_MAX_AGE);
//        cookie.setSecure(true); //HTTPS 배포시엔 true로 설정 변경 필요
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
