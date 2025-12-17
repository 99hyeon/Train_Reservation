package com.example.japtangjjigae.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.example.japtangjjigae.jwt.JWTUtil;
import com.example.japtangjjigae.user.common.OAuthProvider;
import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2SuccessHandlerTest {

    @Mock
    private JWTUtil jwtUtil;

    private CustomOAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp(){
        successHandler = new CustomOAuth2SuccessHandler(jwtUtil);
    }

    @Test
    @DisplayName("principal이 CustomOAuth2User가 아니면 super로 빠지고 JWT 쿠키 없음")
    void principal이_CustomOAuth2User가_아니면_super로_빠지고_JWT쿠키_없다() throws Exception {
        //given
        Object notCustomPrincipal = "just-string-principal";
        Authentication auth = new UsernamePasswordAuthenticationToken(
            notCustomPrincipal, null, Collections.emptyList()
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        successHandler.onAuthenticationSuccess(request, response, auth);

        //then
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
        assertThat(response.getCookie("Authorization")).isNull();
        verify(jwtUtil, never()).createJwt(anyLong(), any(OAuthProvider.class), anyLong());
    }

    @Test
    @DisplayName("로그인 성공 - signupTicket이 null이라는 것은 회원가입을 한 회원이라는 뜻")
    void signupTicket이_null_이라_홈으로_리다이렉트하고_쿠키에_JWT를_심는다() throws Exception {
        //given
        given(jwtUtil.createJwt(anyLong(), any(OAuthProvider.class), anyLong()))
            .willReturn("mock.jwt.token");

        CustomOAuth2User principal = new CustomOAuth2User(
            Map.of("id", "dummy"),
            "KAKAO:1",
            null
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, null, Collections.emptyList()
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        successHandler.onAuthenticationSuccess(request, response, auth);

        //then
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:8080/");

        Cookie cookie = response.getCookie("Authorization");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("mock.jwt.token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(60 * 60);

        verify(jwtUtil).createJwt(eq(1L), eq(OAuthProvider.KAKAO), eq(3600L));
    }

    @Test
    @DisplayName("singupTicket이 존재한다는 것은 최초 소셜로그인이라는 뜻")
    void singupTicket_존재해서_회원가입_하는_URL로_리다이렉트하고_쿠키에_JWT를_심는다() throws Exception {
        //given
        given(jwtUtil.createJwt(anyLong(), any(OAuthProvider.class), anyLong()))
            .willReturn("mock.jwt.token");

        String rawTicket = "t 1+2/=";
        String encoded = URLEncoder.encode(rawTicket, StandardCharsets.UTF_8);

        CustomOAuth2User principal = new CustomOAuth2User(
          Map.of("id", "dummy"),
          "KAKAO:999",
          rawTicket
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, null, Collections.emptyList()
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        successHandler.onAuthenticationSuccess(request, response, auth);

        //then
        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).startsWith(
            "http://localhost:8080/api/v1/check/signup?ticket="
        );
        assertThat(redirectUrl).endsWith(encoded);

        Cookie cookie = response.getCookie("Authorization");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("mock.jwt.token");

        verify(jwtUtil).createJwt(eq(999L), eq(OAuthProvider.KAKAO), eq(3600L));
    }
}