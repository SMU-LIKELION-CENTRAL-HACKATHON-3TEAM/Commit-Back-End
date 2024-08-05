package com.likelion.commit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AnalysisResult {

    //워라밸
    public String wlBalance;

    //잠
    public String sleep;

    //운동
    public String workOut;

    //식사
    public String meal;

    //총평
    public String review;

    public List<TimeTableResponseDto.TimeTableResponse> recommend;
}