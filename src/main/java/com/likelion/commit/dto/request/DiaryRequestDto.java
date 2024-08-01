package com.likelion.commit.dto.request;


import com.likelion.commit.entity.Diary;
import com.likelion.commit.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DiaryRequestDto {
    public LocalDate date;
    public String content;

    public Diary toEntity(){
        return Diary.builder()
                .date(date)
                .content(content)
                .build();
    }
}
