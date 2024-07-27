package com.likelion.commit.entity;


import com.likelion.commit.dto.UpdateRuleSetRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
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
    private String WLBalance;

    @Column
    private String sleepTime;

    @Column
    private String exerciseTime;

    @Column
    private String detail;


    @OneToOne(mappedBy = "ruleSet")
    private User user;


    public void update(UpdateRuleSetRequestDto updateRuleSetRequestDto){
        WLBalance = updateRuleSetRequestDto.getWLBalance();
        sleepTime = updateRuleSetRequestDto.getSleepTime();
        exerciseTime = updateRuleSetRequestDto.getExerciseTime();
        detail = updateRuleSetRequestDto.getDetail();
    }

}
