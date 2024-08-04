package com.likelion.commit.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdatePasswordRequestDto {
    @NotNull
    public String currentPassword;
    @NotNull
    public String newPassword;
}
