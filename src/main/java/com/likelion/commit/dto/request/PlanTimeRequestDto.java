package com.likelion.commit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PlanTimeRequestDto {
    public LocalDateTime startTime;
    public LocalDateTime endTime;

}
