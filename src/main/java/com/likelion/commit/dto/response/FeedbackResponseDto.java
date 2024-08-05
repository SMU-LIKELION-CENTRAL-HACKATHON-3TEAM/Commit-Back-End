package com.likelion.commit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class FeedbackResponseDto {

    //어제의 타임 테이블
    public List<TimeTableResponseDto.TimeTableResponse> yesterday;

    //분석 결과
    public AnalysisResult analysisResult;




}
