package com.likelion.commit.service;


import com.likelion.commit.dto.CreateUserRequestDto;
import com.likelion.commit.dto.UpdatePasswordRequestDto;
import com.likelion.commit.dto.UserResponseDto;
import com.likelion.commit.entity.User;
import com.likelion.commit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public long createUser(CreateUserRequestDto createUserRequestDto){
        User user = createUserRequestDto.toEntity(/*passwordEncoder*/);
        userRepository.save(user);

        return user.getId();
    }

    @Transactional
    public void updatePassword(String email, UpdatePasswordRequestDto updatePasswordRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        user.setPassword(updatePasswordRequestDto.getNewPassword());

//        if(passwordEncoder.matches(user.getPassword(), updatePasswordRequestDto.getCurrentPassword())) {
//            user.setPassword(passwordEncoder.encode(updatePasswordRequestDto.getNewPassword()));
//        }
//        else{
//            throw new InvalidParameterException("현재 비밀번호와 입력한 비밀번호가 다릅니다.");
//        }
    }

    @Transactional
    public void deleteUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        userRepository.deleteById(user.getId());
    }

    public UserResponseDto getUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        return UserResponseDto.from(user);
    }
}
