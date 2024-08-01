package com.likelion.commit.dto.feedback;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GPTMessageDto {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;
}
