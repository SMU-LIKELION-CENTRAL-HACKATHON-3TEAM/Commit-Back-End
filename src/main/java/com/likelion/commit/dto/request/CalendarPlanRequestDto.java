package com.likelion.commit.dto.request;

import com.likelion.commit.entity.PlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CalendarPlanRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String content;
    private PlanType type;
}
