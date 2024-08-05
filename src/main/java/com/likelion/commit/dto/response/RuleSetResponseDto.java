package com.likelion.commit.dto.response;


import com.likelion.commit.entity.RuleSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RuleSetResponseDto {
    public String wlBalance;
    public String sleepTime;
    public String exerciseTime;
    public String detail;

    public static RuleSetResponseDto from(RuleSet ruleSet){
        return RuleSetResponseDto.builder()
                .wlBalance(ruleSet.getWlBalance())
                .sleepTime(ruleSet.getSleepTime())
                .exerciseTime(ruleSet.getExerciseTime())
                .detail(ruleSet.getDetail())
                .build();
    }
}
