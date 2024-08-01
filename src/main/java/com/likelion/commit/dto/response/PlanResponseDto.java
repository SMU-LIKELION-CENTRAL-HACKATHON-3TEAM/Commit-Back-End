package com.likelion.commit.dto.response;

import com.likelion.commit.entity.Plan;
import com.likelion.commit.entity.PlanStatus;
import com.likelion.commit.entity.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PlanResponseDto {
    private Long planId;
    private String content;
    private int priority;
    private PlanType type;
    private LocalDate date;
    private boolean isComplete;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PlanStatus status;
    private boolean isDelayed;
    private Long childPlan;
    private Long userId;


    public PlanResponseDto(Plan plan) {
        this.planId = plan.getId();
        this.date = plan.getDate();
        this.isComplete = plan.isComplete();
        this.startTime = plan.getStartTime();
        this.endTime = plan.getEndTime();
        this.priority = plan.getPriority();
        this.createdAt = plan.getCreatedAt();
        this.updatedAt = plan.getUpdatedAt();
        this.content = plan.getContent();
        this.status = plan.getStatus();
        this.isDelayed = plan.isDelayed();
        this.childPlan = plan.getChildPlan();
        this.type = plan.getType();
        this.userId = plan.getUser().getId();

    }

    // Plan -> PlanResponseDto
    public static PlanResponseDto from(Plan plan) {
        return new PlanResponseDto(plan);
    }

    // List<Plan> -> <PlanResponseDto>
    public static List<PlanResponseDto> from(List<Plan> plans) {
        return plans.stream()
                .map(PlanResponseDto::from)
                .collect(Collectors.toList());
    }
}
