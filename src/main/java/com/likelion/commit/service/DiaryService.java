package com.likelion.commit.service;

import com.likelion.commit.dto.request.DiaryDateRequestDto;
import com.likelion.commit.dto.request.DiaryRequestDto;
import com.likelion.commit.dto.response.DiaryResponseDto;
import com.likelion.commit.entity.Diary;
import com.likelion.commit.entity.User;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.repository.DiaryRepository;
import com.likelion.commit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;


    public DiaryResponseDto getDiary(String email, DiaryDateRequestDto diaryDateRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        Diary diary = diaryRepository.findByUserAndDate(user, diaryDateRequestDto.getDate());

        return DiaryResponseDto.from(diary);
    }
}
