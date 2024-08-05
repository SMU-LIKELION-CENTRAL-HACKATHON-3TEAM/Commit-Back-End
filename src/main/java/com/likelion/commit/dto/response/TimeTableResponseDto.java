package com.likelion.commit.dto.response;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.likelion.commit.entity.FixedPlan;
import com.likelion.commit.entity.PlanType;
import com.likelion.commit.entity.TimeTable;
import com.likelion.commit.util.LocalTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeTableResponseDto {

    public List<TimeTableResponse> fixedPlans;
    public List<TimeTableResponse> plans;



    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeTableResponse {
        public Long planId;
        //    private Long fixedPlanId;

        @JsonDeserialize(using = LocalTimeDeserializer.class)
        public LocalTime startTime;

        @JsonDeserialize(using = LocalTimeDeserializer.class)
        public LocalTime endTime;

        public String content;
        public boolean isFixed;
        public PlanType planType;

        public static  TimeTableResponse from(FixedPlan fixedPlan) {
            return TimeTableResponse.builder()
                    .planId(fixedPlan.getId())
                    .startTime(fixedPlan.getStartTime())
                    .endTime(fixedPlan.getEndTime())
                    .content(fixedPlan.getContent())
                    .isFixed(true)
                    .build();

        }

        public static TimeTableResponse from(TimeTable timeTable) {
            return TimeTableResponse.builder()
                    .planId(timeTable.getPlanId())
                    .startTime(timeTable.getStartTime())
                    .endTime(timeTable.getEndTime())
                    .content(timeTable.getContent())
                    .isFixed(timeTable.isFixed())
                    .planType(timeTable.getPlanType())
                    .build();
        }
    }

}
