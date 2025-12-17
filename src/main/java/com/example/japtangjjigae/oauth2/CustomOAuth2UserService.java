package com.example.japtangjjigae.oauth2;

import com.example.japtangjjigae.global.response.code.AuthResponseCode;
import com.example.japtangjjigae.redis.signup.SignupTicketStore;
import com.example.japtangjjigae.user.common.OAuthProvider;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final long SIGNUP_TICKET_TTL_SECONDS = 600L;

    private final UserRepository userRepository;
    private final SignupTicketStore signupTicketStore;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        ProviderInfo providerInfo = resolveProviderInfo(userRequest, oAuth2User);

        User user = userRepository.findBySocialIdAndOauthProvider(providerInfo.providerId(),
            providerInfo.provider()).orElse(null);

        String principalName = null;
        String ticket = null;

        if (user == null) {
            principalName = providerInfo.provider() + ":" + providerInfo.providerId();
            ticket = createSignupTicket(providerInfo);
        } else {
            principalName = providerInfo.provider() + ":" + user.getId();
        }

        return new CustomOAuth2User(oAuth2User.getAttributes(), principalName, ticket);
    }

    private String createSignupTicket(ProviderInfo info) {
        String ticket = UUID.randomUUID().toString();

        signupTicketStore.save(ticket,
            new SignupTicketStore.SignupTicketValue(info.providerId(), info.provider()),
            SIGNUP_TICKET_TTL_SECONDS);

        return ticket;
    }

    private ProviderInfo resolveProviderInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response response;
        OAuthProvider provider;

        if (registrationId.equals("kakao")) {
            provider = OAuthProvider.KAKAO;
            response = new KakaoResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("naver")) {
            provider = OAuthProvider.NAVER;
            response = new NaverResponse(oAuth2User.getAttributes());
        } else {
            throw oauth2Exception(AuthResponseCode.OAUTH2_UNSUPPORTED_PROVIDER);
        }

        return new ProviderInfo(provider, response.getProviderId());
    }

    private OAuth2AuthenticationException oauth2Exception(AuthResponseCode code) {
        OAuth2Error error = new OAuth2Error(code.name(), code.getMessage(), null);
        return new OAuth2AuthenticationException(error);
    }

    private record ProviderInfo(OAuthProvider provider, String providerId) {

    }

}
