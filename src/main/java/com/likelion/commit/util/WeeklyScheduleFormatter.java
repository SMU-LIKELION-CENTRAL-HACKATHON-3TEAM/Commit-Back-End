package com.likelion.commit.util;

import com.likelion.commit.entity.TimeTable;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class WeeklyScheduleFormatter {

    public static String formatWeeklySchedule(List<TimeTable> timeTables, LocalDate startDate) {
        Map<LocalDate, DailyData> weekData = initializeWeekData(startDate);
        processTimeTables(timeTables, weekData);
        return generateFormattedString(weekData, startDate);
    }

    private static Map<LocalDate, DailyData> initializeWeekData(LocalDate startDate) {
        Map<LocalDate, DailyData> data = new HashMap<>();
        for (int i = 0; i < 7; i++) {  // 7일로 변경
            data.put(startDate.plusDays(i), new DailyData());
        }
        return data;
    }

    private static void processTimeTables(List<TimeTable> timeTables, Map<LocalDate, DailyData> weekData) {
        for (TimeTable timeTable : timeTables) {
            DailyData dailyData = weekData.get(timeTable.getDate());
            if (dailyData != null) {
                updateDailyData(dailyData, timeTable);
            }
        }
    }

    private static void updateDailyData(DailyData dailyData, TimeTable timeTable) {
        long durationMinutes = Duration.between(timeTable.getStartTime(), timeTable.getEndTime()).toMinutes();

        if (timeTable.getPlanType() != null) {
            switch (timeTable.getPlanType()) {
                case LIFE:
                    dailyData.lifeMinutes += durationMinutes;
                    break;
                case WORK:
                    dailyData.workMinutes += durationMinutes;
                    break;
                case EXERCISE:
                    dailyData.workoutMinutes += durationMinutes;
                    break;
            }
        }

        if (timeTable.getContent() != null &&
                (timeTable.getContent().equals("수면 시간") ||
                        timeTable.getContent().equals("수면시간") ||
                        timeTable.getContent().equals("수면"))) {
            dailyData.sleepHours += durationMinutes / 60.0;
        }
    }

    private static String generateFormattedString(Map<LocalDate, DailyData> weekData, LocalDate startDate) {
        String[] daysOfWeek = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
        StringBuilder result = new StringBuilder("사용자의 일정은 다음과 같아. \n");

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DailyData dailyData = weekData.get(currentDate);
            Object[] formattedValues = dailyData.getFormattedValues();

            result.append(String.format("%s: 워라밸 %s, 운동 %d분, 수면 %.1f시간\n",
                    daysOfWeek[i], formattedValues[0], formattedValues[1], formattedValues[2]));
        }

        return result.toString();
    }

    private static class DailyData {
        int lifeMinutes = 0;
        int workMinutes = 0;
        int workoutMinutes = 0;
        double sleepHours = 0.0;

        Object[] getFormattedValues() {
            int totalMinutes = lifeMinutes + workMinutes;
            String workLifeBalance = (totalMinutes == 0) ? "5:5" :
                    String.format("%d:%d", 10 * workMinutes / totalMinutes, 10 * lifeMinutes / totalMinutes);
            return new Object[]{workLifeBalance, workoutMinutes, sleepHours};
        }
    }
}