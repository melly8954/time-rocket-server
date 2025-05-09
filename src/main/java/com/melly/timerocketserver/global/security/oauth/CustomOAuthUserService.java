package com.melly.timerocketserver.global.security.oauth;

import com.melly.timerocketserver.global.security.CustomUserDetails;
import com.melly.timerocketserver.global.security.oauth.response.GoogleResponse;
import com.melly.timerocketserver.global.security.oauth.response.NaverResponse;
import com.melly.timerocketserver.global.security.oauth.response.OAuth2Response;
import com.melly.timerocketserver.domain.entity.Role;
import com.melly.timerocketserver.domain.entity.Status;
import com.melly.timerocketserver.domain.entity.UserEntity;
import com.melly.timerocketserver.domain.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuthUserService extends DefaultOAuth2UserService {
    private UserRepository userRepository;
    public CustomOAuthUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;
        if (registrationId.equals("naver")) {
            // oAuth2User.getAttributes()로 얻은 Map<String, Object> 형태의 데이터를 NaverResponse 객체로 변환
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {
            oAuth2Response = null;
            return null;
        }

        // 리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 사용자가 존재하지 않는 경우 새 사용자 생성
                    UserEntity newUser = UserEntity.builder()
                            .email(email)
                            .nickname(oAuth2Response.getName())
                            .role(Role.USER)
                            .status(Status.ACTIVE)
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                    userRepository.save(newUser);
                    return newUser;
                });

        // 이미 가입된 사용자인데, provider가 다르면 예외 발생
        if (user.getProvider() == null || !user.getProvider().equals(provider)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("provider_mismatch", "기존 계정이 이미 다른 로그인 방식으로 가입되어 있습니다. 일반 로그인을 이용해주세요.", null)
            );
        }

        // 계정 상태 확인
        if (user.getStatus() == Status.DELETED) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_deleted", "탈퇴된 계정입니다. 관리자에게 문의하십시오.", null)
            );
        }

        if (user.getStatus() == Status.INACTIVE) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_inactive", "이 계정은 비활성화 상태입니다. 관리자에게 문의하십시오.", null)
            );
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
