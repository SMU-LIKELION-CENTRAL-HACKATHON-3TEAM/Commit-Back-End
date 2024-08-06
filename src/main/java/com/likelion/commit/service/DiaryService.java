package com.likelion.commit.service;

import com.likelion.commit.dto.request.CreateDiaryRequestDto;
import com.likelion.commit.dto.request.UpdateDiaryRequestDto;
import com.likelion.commit.dto.response.DiaryResponseDto;
import com.likelion.commit.entity.Diary;
import com.likelion.commit.entity.User;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.repository.DiaryRepository;
import com.likelion.commit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;


    @Transactional
    public Long createDiary(String email, CreateDiaryRequestDto createDiaryRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        Diary diary = createDiaryRequestDto.toEntity();
        diary.setUser(user);

        diaryRepository.save(diary);

        return diary.getId();
    }


    public DiaryResponseDto getDiary(String email, LocalDate date){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        Diary diary = diaryRepository.findByUserAndDate(user, date);
        return DiaryResponseDto.from(diary);
    }

    @Transactional
    public void updateDiary(String email, UpdateDiaryRequestDto updateDiaryRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        Diary diary = diaryRepository.findByUserAndDate(user, updateDiaryRequestDto.getDate());

        log.info("수정 id" + diary.getId());
        if(diary.getUser().getEmail().equals(user.getEmail())){
            diary.update(updateDiaryRequestDto);
        }
    }
}
