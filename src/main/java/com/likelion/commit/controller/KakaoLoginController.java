package com.likelion.commit.controller;

import com.likelion.commit.Security.dto.JwtDto;
import com.likelion.commit.Security.userDetails.CustomUserDetails;
import com.likelion.commit.Security.utils.JwtUtil;
import com.likelion.commit.dto.response.KakaoUserInfoResponseDto;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.Location;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "Kakao Login API", description = "Kakao 로그인 관련 API 엔드포인트")
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final JwtUtil jwtUtil;
    @Operation(
            method = "GET",
            summary = "카카오 Callback",
            description = "Kakao OAuth 인증 코드로부터 액세스 토큰을 얻고, 사용자 정보를 가져와 JWT 토큰을 발급합니다.")
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code, HttpServletResponse response) {

        // 액세스 토큰 얻기
        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        // 사용자 정보 얻기
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        // 사용자 등록 또는 기존 사용자 찾기
        JwtDto jwtDto = kakaoService.registerUserFromKakao(userInfo);

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Location", "http://localhost:3000/auth?" +
                "access=" + jwtDto.accessToken +
                "&refresh=" + jwtDto.refreshToken);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}