package com.likelion.commit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdatePasswordRequestDto {
    public String currentPassword;
    public String newPassword;
}