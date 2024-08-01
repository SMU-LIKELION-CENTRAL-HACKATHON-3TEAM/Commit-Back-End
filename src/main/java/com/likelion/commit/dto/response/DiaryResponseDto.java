package com.likelion.commit.dto.response;


import com.likelion.commit.entity.Diary;
import com.likelion.commit.entity.RuleSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class DiaryResponseDto {
    public Long diaryId;
    public String content;

    public static DiaryResponseDto from(Diary diary){
        return DiaryResponseDto.builder()
                .diaryId(diary.getId())
                .content(diary.getContent())
                .build();
    }
}
