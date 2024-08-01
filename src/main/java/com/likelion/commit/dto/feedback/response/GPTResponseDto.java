package com.likelion.commit.dto.feedback.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.commit.dto.feedback.GPTMessageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GPTResponseDto {

    @JsonProperty("id")
    String id;

    @JsonProperty("object")
    String object;

    @JsonProperty("created")
    int created;

    @JsonProperty("model")
    String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        //gpt 대화 인덱스 번호
        @JsonProperty("index")
        private int index;
        // 지피티로 부터 받은 메세지
        // 여기서 content는 유저의 prompt가 아닌 gpt로부터 받은 response
        @JsonProperty("message")
        private GPTMessageDto message;
    }

}
