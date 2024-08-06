package com.likelion.commit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class WeeklyFeedbackResponseDto {

    //워라밸 관련 피드백
    public WeeklyAnalysisResult wlBalance;

    //평균 운동 시간 관련 피드백
    public WeeklyAnalysisResult workOut;

    //수면 시간 관련 피드백
    public WeeklyAnalysisResult sleep;

    //종합 피드백
    public List<String> review;

}
