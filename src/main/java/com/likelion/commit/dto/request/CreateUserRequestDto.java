package com.likelion.commit.dto.request;


import com.likelion.commit.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateUserRequestDto {
    public String name;
    public String password;
    public String email;

    public User toEntity(/*PasswordEncoder passwordEncoder*/){
//        String encodePassword = passwordEncoder.encode(password);
        return User.builder()
                .name(name)
                .email(email)
                .password(password)    // 인증인가 추가 시 encodePassword로 변경
                .build();
        // 카카오 로그인 추가 시 authType 추가
    }
}
