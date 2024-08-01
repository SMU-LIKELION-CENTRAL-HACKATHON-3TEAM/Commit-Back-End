package com.likelion.commit.utils;

import com.likelion.commit.dto.feedback.GPTMessageDto;
import com.likelion.commit.dto.feedback.request.GPTRequestDto;
import com.likelion.commit.dto.feedback.response.GPTResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class GPTUtils {

    @Value("${gpt.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public String createMessage(String prompt) {

        // 프롬프트 메시지
        String assistantContent = "당신은 사용자의 기존 일정을 피드백을 통해 새로운 일정을 추천해주는 역할입니다."
                ;

        GPTMessageDto assistantMessage = GPTMessageDto.builder()
                .role("assistant")
                .content(assistantContent)
                .build();

        StringBuilder stringBuilder = new StringBuilder();

        GPTMessageDto userMessage = GPTMessageDto.builder()
                .role("user")
                .content(stringBuilder.toString())
                .build();

        List<GPTMessageDto> messages = new ArrayList<>();

        messages.add(assistantMessage);
        messages.add(userMessage);
        GPTRequestDto gptRequestDto = GPTRequestDto.builder()
                .model("gpt-4o")
//              .temperature(0.2)
                .messages(messages)
//              .maxTokens(2000)
//              .topP(1.0)
                .build();

        Mono<GPTRequestDto> gptRequestDtoMono = Mono.just(gptRequestDto);

        GPTResponseDto resposne = webClient
                .post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(gptRequestDtoMono, GPTRequestDto.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse
                        -> Mono.error(new RuntimeException(clientResponse.toString())))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse
                        -> Mono.error(new RuntimeException(clientResponse.toString())))
                .bodyToMono(GPTResponseDto.class)
                .block();

        if (resposne.getChoices().isEmpty()) {
            log.info("Choice 없음");
            throw new RuntimeException();
        }

        log.info("response message -> {} ", resposne.getChoices().get(0));
        String result = resposne.getChoices().get(0).getMessage().getContent();
    }
}
