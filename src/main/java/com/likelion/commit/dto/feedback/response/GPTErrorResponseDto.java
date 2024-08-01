package com.likelion.commit.dto.feedback.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GPTErrorResponseDto {

    public ErrorMessage error;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private class ErrorMessage {
        String message;
        String type;
        String param;
        String code;
    }
}
