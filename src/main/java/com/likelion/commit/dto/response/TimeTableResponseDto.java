package com.likelion.commit.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeTableResponseDto {
    private Long planId;
//    private Long fixedPlanId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isWeekend;
    private String content;
    private Long userId;
    private boolean isFixed;
    private int priority;

}
