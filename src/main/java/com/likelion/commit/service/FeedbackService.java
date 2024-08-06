package com.likelion.commit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.likelion.commit.dto.response.*;
import com.likelion.commit.entity.RuleSet;
import com.likelion.commit.entity.TimeTable;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.repository.RuleSetRepository;
import com.likelion.commit.repository.TimeTableRepository;
import com.likelion.commit.repository.UserRepository;
import com.likelion.commit.util.GPTUtil;
import com.likelion.commit.util.WeeklyScheduleFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private final UserRepository userRepository;
    private final TimeTableRepository timeTableRepository;
    private final RuleSetRepository ruleSetRepository;
    private final GPTUtil gptUtil;

    public FeedbackResponseDto createFeedback(String email, LocalDate localDate) {

        Long userId = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED)
        ).getId();

        RuleSet ruleSet = ruleSetRepository.findByUser_Id(userId).orElseThrow(
                () -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED)
        );

        //어제 타임테이블 가져오기
        LocalDate yesterday = localDate.minusDays(1);//생성 기준으로 하루

        List<TimeTable> yesterdayTimeTables = timeTableRepository.findByUser_IdAndDate(userId, yesterday);

        List<TimeTableResponseDto.TimeTableResponse> yesterdayTimeTableRes
                = yesterdayTimeTables.stream().map(TimeTableResponseDto.TimeTableResponse::from).toList();

        //추천 생성
        String prompt = generatePrompt(yesterdayTimeTables, ruleSet);
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

    public WeeklyFeedbackResponseDto createWeeklyFeedback(String email, LocalDate startDate) {
        Long userId = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED)
        ).getId();


        List<TimeTable> timeTables = timeTableRepository
                .findWeekScheduleByUserIdAndStartDate(userId, startDate, startDate.plusDays(6));

        String result = gptUtil.generateMessage(getWeeklyFeedbackAssistant(),
                WeeklyScheduleFormatter.formatWeeklySchedule(timeTables, startDate)
        );
        WeeklyFeedbackResponseDto weeklyFeedbackResponseDto = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            weeklyFeedbackResponseDto = objectMapper.readValue(result, WeeklyFeedbackResponseDto.class);
            log.info("Deserialize 성공. . .");
        } catch (JsonProcessingException exception) {
            log.error("GPT가 이상하게 줌 !! ---> {}", result);
            log.error("GPT 에러 위치 ----> {}", exception.getLocation());
            log.error("GPT 에러 메세지 ----> {}", exception.getMessage());
        }

        return weeklyFeedbackResponseDto;
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

    private String getWeeklyFeedbackAssistant() {
        return "당신은 주간 워크&라이프 밸런스, 운동, 수면 패턴을 분석하는 AI 어시스턴트입니다. 월요일부터 일요일까지 매일의 데이터를 바탕으로 피드백을 제공하는 것이 당신의 임무입니다.\n" +
                "\n" +
                "매일 다음과 같은 데이터를 받게 될 것입니다:\n" +
                "1. 워크&라이프 밸런스 점수 (work:life 형식)\n" +
                "2. 운동 시간 (분 단위)\n" +
                "3. 수면 시간 (시간 단위)\n" +
                "\n" +
                "당신의 응답은 반드시 JSON 형식이어야 하며, json 외에 다른 응답은 포함되지 않아야 합니다. JSON 형식은 다음과 같습니다.\n" +
                "응답에 문자열 `를 절대로 포함하지 마십시오." +
                "\n" +
                "{\n" +
                "  \"wlBalance\": {\n" +
                "    \"value\": \"int:int\",\n" +
                "    \"mon\": \"int:int\",\n" +
                "    \"tue\": \"int:int\",\n" +
                "    \"wed\": \"int:int\",\n" +
                "    \"thu\": \"int:int\",\n" +
                "    \"fri\": \"int:int\",\n" +
                "    \"sat\": \"int:int\",\n" +
                "    \"sun\": \"int:int\",\n" +
                "    \"results\": [\"string\", \"string\"]\n" +
                "  },\n" +
                "  \"workOut\": {\n" +
                "    \"value\": \"float\",\n" +
                "    \"mon\": \"float\",\n" +
                "    \"tue\": \"float\",\n" +
                "    \"wed\": \"float\",\n" +
                "    \"thu\": \"float\",\n" +
                "    \"fri\": \"float\",\n" +
                "    \"sat\": \"float\",\n" +
                "    \"sun\": \"float\",\n" +
                "    \"results\": [\"string\", \"string\"]\n" +
                "  },\n" +
                "  \"sleep\": {\n" +
                "    \"value\": \"float\",\n" +
                "    \"mon\": \"float\",\n" +
                "    \"tue\": \"float\",\n" +
                "    \"wed\": \"float\",\n" +
                "    \"thu\": \"float\",\n" +
                "    \"fri\": \"float\",\n" +
                "    \"sat\": \"float\",\n" +
                "    \"sun\": \"float\",\n" +
                "    \"results\": [\"string\", \"string\"]\n" +
                "  },\n" +
                "  \"review\": [\"string\", \"string\"]\n" +
                "}\n" +
                "\n" +
                "가이드라인:\n" +
                "1. 워크&라이프 밸런스에 대해:\n" +
                "   - 주간 평균 점수를 계산하여 \"value\"에 넣으세요.\n" +
                "   - 각 요일의 워라밸 점수를 해당 요일 필드(mon, tue, 등)에 넣으세요.\n" +
                "   - 결과를 \"int:int\" 형식으로 표현하세요 (예: \"7:3\"은 70% 일, 30% 생활을 의미).\n" +
                "   - 밸런스에 대한 피드백과 개선을 위한 제안을 \"results\"에 제공하세요.\n" +
                "\n" +
                "2. 운동에 대해:\n" +
                "   - 주간 평균 운동 시간을 계산하여 \"value\"에 넣으세요.\n" +
                "   - 각 요일의 운동 시간을 해당 요일 필드에 넣으세요.\n" +
                "   - 모든 값을 소수점 첫째 자리까지 반올림하세요.\n" +
                "   - 운동 루틴의 일관성과 적절성에 대해 \"results\"에 코멘트하세요.\n" +
                "\n" +
                "3. 수면에 대해:\n" +
                "   - 주간 평균 수면 시간을 계산하여 \"value\"에 넣으세요.\n" +
                "   - 각 요일의 수면 시간을 해당 요일 필드에 넣으세요.\n" +
                "   - 모든 값을 소수점 첫째 자리까지 반올림하세요.\n" +
                "   - 수면의 질에 대한 피드백과 개선을 위한 제안을 \"results\"에 제공하세요.\n" +
                "\n" +
                "4. 종합 리뷰에 대해:\n" +
                "   - 한 주에 대한 종합적인 분석을 제공하세요.\n" +
                "   - 더 나은 워크&라이프 밸런스, 체력, 수면 습관을 위한 실행 가능한 조언을 제공하세요.\n" +
                "\n" +
                "피드백은 건설적이고, 개인화되어 있으며, 실행 가능해야 합니다. 제공된 데이터를 사용하여 개인의 특정 상황에 맞는 응답을 제공하세요. 데이터가 없는 날의 경우, 해당 요일 필드에 null을 입력하세요.";
    }



    private String generatePrompt(List<TimeTable> timeTables, RuleSet ruleSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자의 일정 규칙을 알려드리겠습니다.");
        sb.append("워크 & 라이프 밸런스는 ");
        sb.append(ruleSet.getWlBalance()).append("이고, ");
        sb.append("하루 목표 수면시간은");
        sb.append(ruleSet.getSleepTime()).append("시간이고, ");
        sb.append("하루 목표 운동시간은");
        sb.append(ruleSet.getExerciseTime()).append("시간이고, ");
        sb.append("그 외 세부사항은").append("이야.");

        sb.append("어제 일정은 ");
        timeTables.forEach(timeTable -> {
            sb.append(timeTable.getStartTime());
            sb.append("부터 ");
            sb.append(timeTable.getEndTime());
            sb.append(" 까지");
            sb.append(timeTable.getContent());
            sb.append("를 했습니다.");
        }
        );
        return sb.toString();
    }

}
