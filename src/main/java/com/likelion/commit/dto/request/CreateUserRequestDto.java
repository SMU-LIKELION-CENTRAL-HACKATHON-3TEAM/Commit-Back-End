package com.likelion.commit.dto.request;


import com.likelion.commit.entity.AuthType;
import com.likelion.commit.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateUserRequestDto {
    public String name;
    public String password;
    public String email;

    public User toEntity(PasswordEncoder passwordEncoder){
        String encodePassword = passwordEncoder.encode(password);
        return User.builder()
                .name(name)
                .email(email)
                .password(encodePassword)    // 인증인가 추가 시 encodePassword로 변경
                .authType(AuthType.GENERAL)
                .Role("USER")
                .build();
        // 카카오 로그인 추가 시 authType 추가
    }
}
