package com.likelion.commit.entity;


import com.likelion.commit.dto.request.UpdatePlanRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private boolean isComplete;

    private LocalTime startTime;

    private LocalTime endTime;

    private int priority;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String content;

    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    private boolean isDelayed;

    private Long childPlan;

    @Enumerated(EnumType.STRING)
    private PlanType type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private boolean isCalendar;

    public void update(UpdatePlanRequestDto updatePlanRequestDto){
        this.content = updatePlanRequestDto.getContent();
        this.priority = updatePlanRequestDto.getPriority();
        this.type = updatePlanRequestDto.getPlanType();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(PlanStatus planStatus) {
        this.status = planStatus;
    }

    public void setTime(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setDelayToDefault() {
        this.childPlan = null;
        this.isDelayed = false;
    }

    public void setComplete(boolean b) {
        isComplete = b;
    }

    public void setDelayed(boolean b) {
        isDelayed = b;
    }

    public void setChildPlan(Long id) {
        childPlan = id;
    }
    public void setCalendar(boolean b){isCalendar = b;}
}
