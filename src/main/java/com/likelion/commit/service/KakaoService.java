package com.likelion.commit.service;

import com.likelion.commit.Security.dto.JwtDto;
import com.likelion.commit.Security.userDetails.CustomUserDetails;
import com.likelion.commit.Security.utils.JwtUtil;
import com.likelion.commit.dto.response.KakaoTokenResponseDto;
import com.likelion.commit.dto.response.KakaoUserInfoResponseDto;
import com.likelion.commit.entity.AuthType;
import com.likelion.commit.entity.User;
import com.likelion.commit.repository.UserRepository;
import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private String clientId;
    private final String KAUTH_TOKEN_URL_HOST;
    private final String KAUTH_USER_URL_HOST;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public KakaoService(@Value("${kakao.client_id}") String clientId, UserRepository userRepository, JwtUtil jwtUtil) {
        this.clientId = clientId;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        KAUTH_TOKEN_URL_HOST ="https://kauth.kakao.com";
        KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    }
    public String getAccessTokenFromKakao(String code) {

        KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();


        log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.getRefreshToken());
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoTokenResponseDto.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDto.getScope());

        return kakaoTokenResponseDto.getAccessToken();
    }

    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {

        KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
        log.info("[ Kakao Service ] email ---> {} ", userInfo.getKakaoAccount().getEmail());

        return userInfo;
    }



    @Transactional
    public JwtDto registerUserFromKakao(KakaoUserInfoResponseDto userInfo) {
        // 이메일로 기존 사용자를 찾음
        Optional<User> existingUserOpt = userRepository.findByEmail(userInfo.getKakaoAccount().getEmail());

        User user;
        if (existingUserOpt.isPresent()) {
            // 이미 등록된 사용자
            user = existingUserOpt.get();
        } else {
            // 새로운 사용자 등록
            user = User.builder()
                    .name(userInfo.getKakaoAccount().getProfile().getNickName())
                    .email(userInfo.getKakaoAccount().getEmail())
                    .password(null)  // 비밀번호는 필요 없음
                    .authType(AuthType.KAKAO)
                    .Role("USER") // 권한
                    .build();

            user = userRepository.save(user);
        }

        // CustomUserDetails 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );

        // JWT 생성
        String accessToken = jwtUtil.createJwtAccessToken(userDetails);
        String refreshToken = jwtUtil.createJwtRefreshToken(userDetails);

        // JwtDto 생성 및 반환
        return new JwtDto(accessToken, refreshToken);
    }
}