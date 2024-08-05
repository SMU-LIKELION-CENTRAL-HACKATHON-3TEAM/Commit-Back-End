package com.likelion.commit.dto.request;


import com.likelion.commit.entity.RuleSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateRuleSetRequestDto {
    public String wlBalance;
    public String sleepTime;
    public String exerciseTime;
    public String detail;

    public RuleSet toEntity(){
        return RuleSet.builder()
                .wlBalance(wlBalance)
                .sleepTime(sleepTime)
                .exerciseTime(exerciseTime)
                .detail(detail)
                .build();
    }
}
