package com.likelion.commit.dto;


import com.likelion.commit.entity.RuleSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateRuleSetRequestDto {

    public String WLBalance;
    public String sleepTime;
    public String exerciseTime;
    public String detail;

    public RuleSet toEntity(){
        return RuleSet.builder()
                .WLBalance(WLBalance)
                .sleepTime(sleepTime)
                .exerciseTime(exerciseTime)
                .detail(detail)
                .build();
    }
}
