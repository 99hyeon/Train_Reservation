package com.example.japtangjjigae.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.example.japtangjjigae.jwt.JWTUtil;
import com.example.japtangjjigae.jwt.TokenCategory;
import com.example.japtangjjigae.jwt.TokenCookie;
import com.example.japtangjjigae.jwt.TokenTTL;
import com.example.japtangjjigae.token.repository.RefreshTokenRepository;
import com.example.japtangjjigae.user.common.OAuthProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
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
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private ObjectMapper objectMapper;
    private CustomOAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        successHandler = new CustomOAuth2SuccessHandler(jwtUtil, objectMapper,
            refreshTokenRepository);
    }

    @Test
    @DisplayName("principal이 CustomOAuth2User가 아니면 토큰 발급/쿠키 세팅이 일어나지 않는다")
    void principal이_CustomOAuth2User가__아니면_토큰발급_안함() throws Exception {
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
        assertThat(response.getHeader("Set-Cookie")).isNull();

        verify(jwtUtil, never()).createJwt(any(TokenCategory.class), anyLong(),
            any(OAuthProvider.class), anyLong());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 - signupTicket이 null이라는 것은 회원가입을 한 회원이라는 뜻 / access, refresh 토큰 존재")
    void signupTicket이_null_이라_홈으로_리다이렉트하고_쿠키에_JWT를_심는다() throws Exception {
        //given
        given(jwtUtil.createJwt(eq(TokenCategory.REFRESH), eq(1L), eq(OAuthProvider.KAKAO), eq(TokenTTL.REFRESH.seconds())))
            .willReturn("mock.refresh.jwt");
        given(jwtUtil.createJwt(eq(TokenCategory.ACCESS), eq(1L), eq(OAuthProvider.KAKAO), eq(TokenTTL.ACCESS.seconds())))
            .willReturn("mock.access.jwt");

        CustomOAuth2User principal = new CustomOAuth2User(
            Map.of("id", "dummy"),
            "KAKAO:1",
            1L,
            OAuthProvider.KAKAO,
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
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains(TokenCookie.REFRESH_NAME + "=" + "mock.refresh.jwt");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/");

        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer mock.access.jwt");

        Map<String, Object> body = objectMapper.readValue(
            response.getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(body.get("status")).isEqualTo("LOGIN_SUCCESS");
        assertThat(body.get("accessToken")).isEqualTo("Bearer mock.access.jwt");

        verify(jwtUtil).createJwt(eq(TokenCategory.REFRESH), eq(1L), eq(OAuthProvider.KAKAO), eq(TokenTTL.REFRESH.seconds()));
        verify(jwtUtil).createJwt(eq(TokenCategory.ACCESS), eq(1L), eq(OAuthProvider.KAKAO), eq(TokenTTL.ACCESS.seconds()));
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("singupTicket이 존재한다는 것은 최초 소셜로그인이라는 뜻")
    void singupTicket_존재해서_회원가입_하는_URL로_리다이렉트하고_쿠키에_JWT를_심는다() throws Exception {
        //given
        String rawTicket = "t 1+2/=";

        CustomOAuth2User principal = new CustomOAuth2User(
            Map.of("id", "dummy"),
            "KAKAO:999",
            null,               // 아직 내 서비스 userId 없음
            OAuthProvider.KAKAO,
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
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");

        assertThat(response.getHeader("Set-Cookie")).isNull();
        assertThat(response.getHeader("Authorization")).isNull();

        Map<String, Object> body = objectMapper.readValue(
            response.getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(body.get("status")).isEqualTo("NEED_SIGNUP");
        assertThat(body.get("ticket")).isEqualTo(rawTicket);

        verify(jwtUtil, never()).createJwt(
            any(TokenCategory.class), anyLong(), any(OAuthProvider.class), anyLong()
        );
        verify(refreshTokenRepository, never()).save(any());
    }
}