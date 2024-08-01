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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final FixedPlanRepository fixedPlanRepository;
//    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final PlanService planService;


    @Transactional
    public long createUser(CreateUserRequestDto createUserRequestDto){
        User user = createUserRequestDto.toEntity(/*passwordEncoder*/);
        userRepository.save(user);
        planService.createDefaultFixedPlans(user);
        return user.getId();
    }

    @Transactional
    public void updatePassword(String email, UpdatePasswordRequestDto updatePasswordRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        userRepository.deleteById(user.getId());
    }

    public UserResponseDto getUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        return UserResponseDto.from(user);
    }
}
