package com.likelion.commit.util;

import com.likelion.commit.dto.gpt.GPTMessageDto;
import com.likelion.commit.dto.gpt.request.GPTRequestDto;
import com.likelion.commit.dto.gpt.response.GPTResponseDto;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
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
public class GPTUtil {

    @Value("${gpt.api.url}")
    public String GPT_API_URL;

    @Value("${gpt.api.key}")
    public String GPT_API_KEY;

    //주어진 내용(content)
    public String generateMessage(String assistant, String prompt){


        GPTMessageDto assistantMessage = GPTMessageDto.builder()
                .role("assistant")
                .content(assistant)
                .build();

        GPTMessageDto userMessage = GPTMessageDto.builder()
                .role("user")
                .content(prompt)
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

        log.info("url : {}", GPT_API_URL);

        GPTResponseDto resposne = WebClient.create(GPT_API_URL)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v1")
                        .path("/chat")
                        .path("/completions")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + GPT_API_KEY)
                .body(gptRequestDtoMono, GPTRequestDto.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse
                        -> Mono.error(new CustomException(ErrorCode.GPT_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse
                        -> Mono.error(new CustomException(ErrorCode.GPT_ERROR)))
                .bodyToMono(GPTResponseDto.class)
                .block();

        if (resposne.getChoices().isEmpty()) {
            log.warn("Choice 없음");
        }

        log.info("response message -> {} ", resposne.getChoices().get(0).getMessage());
        String result = resposne.getChoices().get(0).getMessage().getContent();


        return result;
    }

}
