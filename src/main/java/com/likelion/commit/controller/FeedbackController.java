package com.likelion.commit.controller;

import com.likelion.commit.gloabal.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Operation(summary = "피드백 받기", description = "사용자 정보에 맞추어 피드백을 생성합니다.")
    @GetMapping("")
    public ApiResponse<?> getFeedback(@AuthenticationPrincipal UserDetails userDetails) {

        return ApiResponse.onSuccess(null);
    }
}
