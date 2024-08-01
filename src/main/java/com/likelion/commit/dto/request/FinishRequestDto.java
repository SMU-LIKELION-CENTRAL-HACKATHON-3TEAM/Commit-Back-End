package com.likelion.commit.dto.request;


import com.likelion.commit.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class FinishRequestDto {
    public LocalDate date;
    public String content;

    public Diary toEntity(){
        return Diary.builder()
                .date(date)
                .content(content)
                .build();
    }
}
