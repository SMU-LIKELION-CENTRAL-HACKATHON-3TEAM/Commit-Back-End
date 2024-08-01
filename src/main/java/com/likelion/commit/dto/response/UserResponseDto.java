package com.likelion.commit.dto.response;


import com.likelion.commit.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserResponseDto {
    public Long userId;
    public String name;
    public String email;

    public static UserResponseDto from(User user){
        return UserResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
