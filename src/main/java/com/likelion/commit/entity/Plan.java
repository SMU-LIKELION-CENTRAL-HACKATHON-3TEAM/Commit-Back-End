package com.likelion.commit.entity;


import com.likelion.commit.dto.request.UpdatePlanRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
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

    private LocalDateTime startTime;

    private LocalDateTime endTime;

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


    public void update(UpdatePlanRequestDto updatePlanRequestDto){
        this.content = updatePlanRequestDto.getContent();
        this.priority = updatePlanRequestDto.getPriority();
        this.type = updatePlanRequestDto.getPlanType();
    }
}
