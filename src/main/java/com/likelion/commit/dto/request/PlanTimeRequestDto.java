package com.likelion.commit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PlanTimeRequestDto {
    public LocalTime startTime;
    public LocalTime endTime;

}
