package com.likelion.commit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.likelion.commit.dto.response.AnalysisResult;
import com.likelion.commit.dto.response.FeedbackResponseDto;
import com.likelion.commit.dto.response.TimeTableResponseDto;
import com.likelion.commit.entity.TimeTable;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.repository.TimeTableRepository;
import com.likelion.commit.repository.UserRepository;
import com.likelion.commit.util.GPTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private final UserRepository userRepository;
    private final TimeTableRepository timeTableRepository;
    private final GPTUtil gptUtil;

    public FeedbackResponseDto createFeedback(String email, LocalDate localDate) {

        Long userId = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED)
        ).getId();

        //어제 타임테이블 가져오기
        LocalDate yesterday = localDate.minusDays(1);//생성 기준으로 하루

        List<TimeTable> yesterdayTimeTables = timeTableRepository.findByUser_IdAndDate(userId, yesterday);

        List<TimeTableResponseDto.TimeTableResponse> yesterdayTimeTableRes
                = yesterdayTimeTables.stream().map(TimeTableResponseDto.TimeTableResponse::from).toList();

        //추천 생성
        String prompt = generatePrompt(yesterdayTimeTables);
        log.info("prompt ---> {}", prompt);
        String result = gptUtil.generateMessage(getAssistant(), prompt);
        AnalysisResult analysisResult = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .registerModule(new JavaTimeModule());

            analysisResult = objectMapper.readValue(result, AnalysisResult.class);
            log.info("Deserialize 성공. . .");
        } catch (JsonProcessingException exception) {
            log.error("GPT가 이상하게 줌 !! ---> {}", result);
            log.error("GPT 에러 위치 ----> {}", exception.getLocation());
            log.error("GPT 에러 메세지 ----> {}", exception.getMessage());
        }

        return FeedbackResponseDto.builder()
                .yesterday(yesterdayTimeTableRes)
                .analysisResult(analysisResult)
                .build();
    }

    private String getAssistant() {
        return "사용자의 어제 일정과 피드백을 바탕으로 새로운 일정을 추천하세요. " +
                "수면시간, 아침식사, 점심식사, 저녁식사는 isFixed를 true로, 나머지는 false로 응답하세요." +
                "planId는 null로 두세요." +
                "planType은 enum으로, LIFE, WORK, EXERCISE 중에 하나입니다." +
                "워라밸, 수면, 운동, 식사에 대한 분석과 총평, 그리고 추천 일정을 포함한 JSON 형식으로 응답하세요. 응답 형식은 다음과 같아야 합니다:\n" +
                "\n" +
                "{\n" +
                "  \"wlBalance\": \"워라밸 분석\",\n" +
                "  \"sleep\": \"수면 분석\",\n" +
                "  \"workOut\": \"운동 분석\",\n" +
                "  \"meal\": \"식사 분석\",\n" +
                "  \"review\": \"총평\",\n" +
                "  \"recommend\": [\n" +
                "    {\n" +
                "      \"planId\": 숫자,\n" +
                "      \"startTime\": \"HH:mm\",\n" +
                "      \"endTime\": \"HH:mm\",\n" +
                "      \"content\": \"일정 내용\",\n" +
                "      \"isFixed\": true/false,\n" +
                "      \"planType\": enum\n" +
                "    },\n" +
                "    ...\n" +
                "  ]\n" +
                "}\n" +
                "\n" +
                "각 필드에 대한 분석은 간결하게 작성하고, 추천 일정은 최소 3개 이상 포함하세요. 모든 시간은 HH:mm 형식을 사용하세요. ";
    }

    private String generatePrompt(List<TimeTable> timeTables) {
        StringBuilder sb = new StringBuilder();
        sb.append("어제 일정은 ");
        timeTables.forEach(timeTable -> {
            sb.append(timeTable.getStartTime());
            sb.append("부터 ");
            sb.append(timeTable.getEndTime());
            sb.append(" 까지");
            sb.append(timeTable.getContent());
            sb.append("를 했어. ");
        }
        );
        return sb.toString();
    }

}
