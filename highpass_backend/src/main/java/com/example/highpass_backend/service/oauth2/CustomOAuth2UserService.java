package com.example.highpass_backend.service.oauth2;

import com.example.highpass_backend.entity.auth.OAuth2Account;
import com.example.highpass_backend.entity.auth.OAuthProvider;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.auth.OAuth2AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2AccountRepository oauth2AccountRepository;

    @Override
    @Transactional(readOnly = true)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuth2UserInfo userInfo;
        OAuthProvider provider;

        if ("google".equals(registrationId)) {
            userInfo = new GoogleUserInfo(attributes);
            provider = OAuthProvider.GOOGLE;
        } else if ("kakao".equals(registrationId) || "kakao-calendar".equals(registrationId)) {
            userInfo = new KakaoUserInfo(attributes);
            provider = OAuthProvider.KAKAO;
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        String providerId = userInfo.getProviderId();

        Optional<OAuth2Account> accountOpt =
                oauth2AccountRepository.findByProviderAndProviderId(provider, providerId);

        if (accountOpt.isPresent()) {
            User user = accountOpt.get().getUser();

            return OAuth2UserPrincipal.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole().name())
                    .provider(provider)
                    .providerId(providerId)
                    .isNew(false)
                    .attributes(attributes)
                    .build();
        }

        return OAuth2UserPrincipal.builder()
                .userId(null)
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .provider(provider)
                .providerId(providerId)
                .isNew(true)
                .attributes(attributes)
                .build();
    }
}
