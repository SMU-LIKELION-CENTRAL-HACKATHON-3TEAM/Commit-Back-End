package com.likelion.commit.controller;

import com.likelion.commit.dto.response.FeedbackResponseDto;
import com.likelion.commit.gloabal.response.ApiResponse;
import com.likelion.commit.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "피드백 받기", description = "사용자 정보에 맞추어 피드백을 생성합니다. " +
            "생성하려는 날의 날짜를 인자로 전달합니다.")
    @GetMapping("")
    public ApiResponse<?> getFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam("date")LocalDate localDate) {

        FeedbackResponseDto res = feedbackService.createFeedback(userDetails.getUsername(), localDate);
        return ApiResponse.onSuccess(res);
    }
}
