package com.likelion.commit.entity;


import com.likelion.commit.dto.request.UpdateRuleSetRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ruleSet")
public class RuleSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String wlBalance;

    @Column
    private String sleepTime;

    @Column
    private String exerciseTime;

    @Column
    private String detail;


    @OneToOne(mappedBy = "ruleSet")
    private User user;


    public void update(UpdateRuleSetRequestDto updateRuleSetRequestDto){
        wlBalance = updateRuleSetRequestDto.getWlBalance();
        sleepTime = updateRuleSetRequestDto.getSleepTime();
        exerciseTime = updateRuleSetRequestDto.getExerciseTime();
        detail = updateRuleSetRequestDto.getDetail();
    }

    public void setUser(User user) {
        this.user = user;
    }
}
