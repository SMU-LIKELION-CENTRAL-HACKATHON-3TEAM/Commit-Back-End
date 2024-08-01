package com.likelion.commit.dto.request;


import com.likelion.commit.entity.Plan;
import com.likelion.commit.entity.PlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreatePlanRequestDto {
    public LocalDate date;
    public String content;
    public int priority;
    public PlanType type;

    public Plan toEntity(){
        return Plan.builder()
                .date(date)
                .content(content)
                .priority(priority)
                .type(type)
                .build();
    }
}
