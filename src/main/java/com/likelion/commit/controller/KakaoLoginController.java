package com.likelion.commit.controller;

import com.likelion.commit.dto.response.KakaoUserInfoResponseDto;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        try {
            // 액세스 토큰 얻기
            String accessToken = kakaoService.getAccessTokenFromKakao(code);
            log.info("토큰 가져오기 성공");
            // 사용자 정보 얻기
            KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
            log.info("사용자 정보 가져오기 성공");
            // 사용자 등록 또는 기존 사용자 찾기
            Long userId = kakaoService.registerUserFromKakao(userInfo);
            log.info("사용자 생성 성공");
            // 성공적으로 사용자 ID 반환
            return ResponseEntity.ok(userId);

        } catch (CustomException e) {
            // 사용자 정의 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            // 일반 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버에서 오류가 발생했습니다.");
        }
    }
}