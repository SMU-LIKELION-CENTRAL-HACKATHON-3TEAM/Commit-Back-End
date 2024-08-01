package com.likelion.commit.service;


import com.likelion.commit.dto.request.*;
import com.likelion.commit.dto.response.PlanResponseDto;
import com.likelion.commit.dto.response.TimeTableResponseDto;
import com.likelion.commit.entity.*;
import com.likelion.commit.gloabal.exception.CustomException;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final FixedPlanRepository fixedPlanRepository;

    private final TimeTableRepository timeTableRepository;
    private final DiaryRepository diaryRepository;


    public void createDefaultFixedPlans(User user) {

            // 주말 FixedPlans
            createFixedPlan(user,true, "02:00", "10:00", "수면시간");
            createFixedPlan(user,true, "10:00", "11:00", "아침식사");
            createFixedPlan(user,true, "14:00", "15:00", "점심식사");
            createFixedPlan(user,true, "19:00", "20:00", "저녁식사");

            // 평일 FixedPlans
            createFixedPlan(user,false, "00:00", "07:00", "수면시간");
            createFixedPlan(user, false, "08:00", "09:00", "아침식사");
            createFixedPlan(user, false, "13:00", "14:00", "점심식사");
            createFixedPlan(user, false, "17:30", "18:30", "저녁식사");

    }

    private void createFixedPlan(User user, boolean isWeekend, String startTime, String endTime, String content) {
        FixedPlan fixedPlan = new FixedPlan();
        fixedPlan.setUser(user);
        fixedPlan.setStartTime(LocalTime.parse(startTime));
        fixedPlan.setEndTime(LocalTime.parse(endTime));
        fixedPlan.setWeekend(isWeekend);
        fixedPlan.setContent(content);

        fixedPlanRepository.save(fixedPlan);
    }


    @Transactional
    public List<PlanResponseDto> createPlans(String email,
                                             List<CreatePlanRequestDto> createPlanRequestDtoList) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        List<Plan> plans = createPlanRequestDtoList.stream().map(dto -> {
            Plan plan = dto.toEntity();
            plan.setUser(user);
            return plan;
        }).collect(Collectors.toList());

        List<Plan> savedPlan = planRepository.saveAll(plans);

        return savedPlan.stream()
                .map(PlanResponseDto::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public void updatePlans(String email, List<UpdatePlanRequestDto> updatePlanRequestDtos) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        List<Plan> plans = updatePlanRequestDtos.stream().map(dto -> {
            Plan plan = planRepository.findById(dto.getPlanId()).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));
            plan.update(dto);
            return plan;
        }).collect(Collectors.toList());

        List<Plan> savedPlan = planRepository.saveAll(plans);
    }

    @Transactional
    public void deletePlans(String email, List<DeletePlanRequestDto> deletePlanRequestDtos) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        deletePlanRequestDtos.forEach(dto -> {
            planRepository.deleteById(dto.getPlanId());
        });
    }

    @Transactional
    public void completePlan(String email, Long planId, PlanTimeRequestDto planTimeRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));



        if (plan.getUser().getEmail().equals(user.getEmail())) {
            // 상태가 null이면 COMPLETE로 설정
            if (plan.getStatus() == null) {
                plan.setStatus(PlanStatus.COMPLETE);
            }

            if (plan.getStatus().equals(PlanStatus.DELAYED)) {
                planRepository.deleteById(plan.getChildPlan());
                plan.setChildPlan(null);
                plan.setDelayed(false);
            }
            plan.setStartTime(planTimeRequestDto.startTime);
            plan.setEndTime(planTimeRequestDto.endTime);
            plan.setStatus(PlanStatus.COMPLETE);
            plan.setComplete(true);

            //TODO : 이미 TimeTable에 들어가 있는 경우 -> Exception

            timeTableRepository.save(TimeTable.builder()
                    .planId(plan.getId())
                    .date(plan.getDate())
                    .startTime(plan.getStartTime())
                    .endTime(plan.getEndTime())
                    .priority(plan.getPriority())
                    .content(plan.getContent())
                    .isFixed(false)
                    .user(user)
                    .build()
            );
        } else throw new CustomException(ErrorCode.UNAUTHORIZED_401);
    }

    @Transactional
    public void cancelPlan(String email, Long planId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));



        if (plan.getUser().getEmail().equals(user.getEmail())) {
            if (plan.getStatus() == null) {
                plan.setStatus(PlanStatus.CANCELED);
            }
            if (plan.getStatus().equals(PlanStatus.DELAYED)) {
                planRepository.deleteById(plan.getChildPlan());
                plan.setChildPlan(null);
                plan.setDelayed(false);
            }
            plan.setStatus(PlanStatus.CANCELED);
            plan.setComplete(false);
        }
    }

    @Transactional
    public void delayPlan(String email, Long planId, DelayPlanRequestDto delayPlanRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));



        if (plan.getUser().getEmail().equals(user.getEmail())) {
            plan.setStatus(PlanStatus.DELAYED);
            plan.setDelayed(true);
            plan.setComplete(false);

            Plan childPlan = Plan.builder()
                    .date(delayPlanRequestDto.getDelayedDate())
                    .content(plan.getContent())
                    .priority(plan.getPriority())
                    .type(plan.getType())
                    .user(user)
                    .build();

            planRepository.save(childPlan);

            plan.setChildPlan(childPlan.getId());

            planRepository.save(plan);
        }
    }


    @Transactional
    public void updateAddedTime(String email, Long planId, PlanTimeRequestDto planTimeRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));

        if (plan.getUser().getEmail().equals(user.getEmail())) {
            plan.setStartTime(planTimeRequestDto.getStartTime());
            plan.setEndTime(planTimeRequestDto.getEndTime());
        }
    }

    @Transactional
    public void updateFixedTime(String email, Long fixedPlanId, PlanTimeRequestDto planTimeRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        FixedPlan fixedPlan = fixedPlanRepository.findById(fixedPlanId).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));

        LocalDate date = LocalDate.now();    // 지금 기준 변경

        LocalTime newStartTime = planTimeRequestDto.getStartTime();
        LocalTime newEndTime = planTimeRequestDto.getEndTime();

        fixedPlan.setStartTime(newStartTime);
        fixedPlan.setEndTime(newEndTime);

        fixedPlanRepository.save(fixedPlan);
    }

    public List<PlanResponseDto> getPlans(String email, PlanDateRequestDto planDateRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        List<Plan> plans = planRepository.findByDateAndUser_Email(planDateRequestDto.getDate(), email);
        return PlanResponseDto.from(plans);
    }


    @Transactional
    public TimeTableResponseDto getTimeTable(String email, PlanDateRequestDto planDateRequestDto) {
        LocalDate date = planDateRequestDto.getDate();
        boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));


        List<TimeTable> timeTables = timeTableRepository.findByUser_IdAndDate(user.getId(), date);

        if (timeTables.isEmpty()) {
            //미래인 경우
            // 저장된 Time Table이 없는 경우
            return TimeTableResponseDto.builder()
                    .fixedPlans(fixedPlanRepository.findByUser_EmailAndIsWeekend(email, isWeekend)
                            .stream()
                            .map(TimeTableResponseDto.TimeTableResponse::from)
                            .toList())
                    .build();
        } else {
            // 지난 날 또는 오늘인 경우
            // 저장된 Time Table이 있는 경우
            List<TimeTableResponseDto.TimeTableResponse> fixedPlans = timeTables.stream()
                    .filter(TimeTable::isFixed)
                    .map(TimeTableResponseDto.TimeTableResponse::from)
                    .toList();

            List<TimeTableResponseDto.TimeTableResponse> plans = timeTables.stream()
                    .filter(timeTable -> !timeTable.isFixed())
                    .map(TimeTableResponseDto.TimeTableResponse::from)
                    .toList();

            //fixedPlans 가 없는 경우 = plans만 있는 경우
            if (fixedPlans.isEmpty()) {
                fixedPlans = fixedPlanRepository.findByUser_EmailAndIsWeekend(email, isWeekend)
                        .stream()
                        .map(TimeTableResponseDto.TimeTableResponse::from)
                        .toList();
            }
            return TimeTableResponseDto.builder()
                    .fixedPlans(fixedPlans)
                    .plans(plans)
                    .build();
        }

    }






    @Transactional
    public List<PlanResponseDto> getUpcomingPlans(String email) {
        LocalDate currentDate = LocalDate.now();
        List<Plan> plans = planRepository.findUpcomingPlans(currentDate, email);

        return plans.stream()
                .limit(3) // 가까운 3개의 일정만 가져오기
                .map(PlanResponseDto::new)
                .collect(Collectors.toList());
    }



    @Transactional
    public List<PlanResponseDto> createCalendarPlans(String email, CalendarPlanRequestDto calendarPlanRequestDto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        LocalDate startDate = calendarPlanRequestDto.getStartDate();
        LocalDate endDate = calendarPlanRequestDto.getEndDate();

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일자가 종료일자보다 나중일 수 없습니다.");
        }



        List<Plan> plans = new ArrayList<>();

        while(!startDate.isAfter(endDate)){
            Plan plan = Plan.builder()
                    .content(calendarPlanRequestDto.getContent())
                    .type(calendarPlanRequestDto.getType())
                    .user(user)
                    .priority(1)     // A로 고정
                    .type(calendarPlanRequestDto.getType())
                    .date(startDate)
                    .build();

            plans.add(plan);

            startDate = startDate.plusDays(1);
        }
        planRepository.saveAll(plans);

        return PlanResponseDto.from(plans);
    }

    @Transactional
    public Long finish(String email, FinishRequestDto finishRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        LocalDate date = finishRequestDto.getDate();
        boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);

        // 고정 Plan만 저장
        // Custom Plan들은 Complete에서 저장함.
        List<FixedPlan> fixedPlans = fixedPlanRepository.findByUser_EmailAndIsWeekend(email, isWeekend);

        List<TimeTable> timeTables = new ArrayList<>();
        for (FixedPlan fixedPlan : fixedPlans) {
            TimeTable timeTable = TimeTable.builder()
                    .date(date)
                    .startTime(fixedPlan.getStartTime())
                    .endTime(fixedPlan.getEndTime())
                    .priority(0)
                    .content(fixedPlan.getContent())
                    .isFixed(true)
                    .planId(fixedPlan.getId()) // FixedPlan의 ID를 저장
                    .user(fixedPlan.getUser())
                    .build();
            timeTables.add(timeTable);
        }

        timeTableRepository.saveAll(timeTables);
        Diary diary = finishRequestDto.toEntity();
        diary.setUser(user);
        diaryRepository.save(diary);

        return diary.getId();
    }


    public List<PlanResponseDto> getMonthlyPlans(String email, YearMonth yearMonth) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        LocalDate startDate = yearMonth.atDay(1); // 해당 월의 첫날
        LocalDate endDate = yearMonth.atEndOfMonth(); // 해당 월의 마지막 날

        List<Plan> plans = planRepository.findByUser_EmailAndDateBetween(email, startDate, endDate);

        return PlanResponseDto.from(plans);
    }

}
