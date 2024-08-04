package com.likelion.commit.entity;


import com.likelion.commit.dto.response.TimeTableResponseDto;
import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "fixedPlan")
public class FixedPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime;

    private LocalTime endTime;

    private boolean isWeekend;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    public void updateTime(LocalTime newStartTime, LocalTime newEndTime) {
        startTime = newStartTime;
        endTime = newEndTime;
    }
}
