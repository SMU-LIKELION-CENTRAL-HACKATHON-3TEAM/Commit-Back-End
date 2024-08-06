package com.likelion.commit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class WeeklyAnalysisResult {

    //값. 주중 평균 워라밸일 경우는 항상 int:int 형식이고, 평균 운동시간과 평균 수면시간은 float 에서 첫 번째 소수점 자리까지
    public String value;

    public String mon;
    public String tue;
    public String wed;
    public String thu;
    public String fri;
    public String sat;
    public String sun;


    //분석 결과와 피드백
    public List<String> results;
}