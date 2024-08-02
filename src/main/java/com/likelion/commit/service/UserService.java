package com.likelion.commit.service;


import com.likelion.commit.dto.request.CreateUserRequestDto;
import com.likelion.commit.dto.request.UpdatePasswordRequestDto;
import com.likelion.commit.dto.response.UserResponseDto;
import com.likelion.commit.entity.User;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.repository.FixedPlanRepository;
import com.likelion.commit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final FixedPlanRepository fixedPlanRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final PlanService planService;


    @Transactional
    public long createUser(CreateUserRequestDto createUserRequestDto){
        User user = createUserRequestDto.toEntity(passwordEncoder);
        userRepository.save(user);
        planService.createDefaultFixedPlans(user);
        return user.getId();
    }

    @Transactional
    public void updatePassword(String email, UpdatePasswordRequestDto updatePasswordRequestDto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        // 현재 비밀번호가 맞는지 확인
        if (passwordEncoder.matches(updatePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
            // 새로운 비밀번호 인코딩 후 저장
            user.setPassword(passwordEncoder.encode(updatePasswordRequestDto.getNewPassword()));
            userRepository.save(user); // 변경된 비밀번호를 저장
        } else {
            throw new InvalidParameterException("현재 비밀번호와 입력한 비밀번호가 다릅니다.");
        }
    }


    @Transactional
    public void deleteUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        userRepository.deleteById(user.getId());
    }

    public UserResponseDto getUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        return UserResponseDto.from(user);
    }
}
