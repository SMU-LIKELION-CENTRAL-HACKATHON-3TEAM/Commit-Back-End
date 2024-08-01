package com.likelion.commit.dto.request;

import com.likelion.commit.entity.PlanType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdatePlanRequestDto {
    public Long planId;
    public String content;
    public int priority;
    public PlanType planType;
}
