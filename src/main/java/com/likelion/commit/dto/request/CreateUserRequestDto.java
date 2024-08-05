package com.likelion.commit.dto.request;


import com.likelion.commit.entity.AuthType;
import com.likelion.commit.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateUserRequestDto {
    public String name;
    @NotBlank(message = "[ERROR] 이메일 입력은 필수입니다.")
    public String email;

    @NotBlank(message = "[ERROR] 비밀번호 입력은 필수 입니다.")
    public String password;

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
