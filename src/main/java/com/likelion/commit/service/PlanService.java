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

import java.time.*;
import java.util.ArrayList;
import java.util.List;
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
        FixedPlan fixedPlan = FixedPlan.builder()
                .user(user)
                .isWeekend(isWeekend)
                .startTime(LocalTime.parse(startTime))
                .endTime(LocalTime.parse(endTime))
                .content(content)
                .build();
        fixedPlanRepository.save(fixedPlan);
    }


    @Transactional
    public List<PlanResponseDto> createPlans(String email,
                                             List<CreatePlanRequestDto> createPlanRequestDtoList) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

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
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        List<Plan> plans = updatePlanRequestDtos.stream().map(dto -> {
            Plan plan = planRepository.findById(dto.getPlanId()).orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));
            plan.update(dto);
            return plan;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deletePlans(String email, List<DeletePlanRequestDto> deletePlanRequestDtos) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        deletePlanRequestDtos.forEach(dto -> {
            planRepository.deleteById(dto.getPlanId());
        });
    }

    @Transactional
    public void completePlan(String email, Long planId, PlanTimeRequestDto planTimeRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));



        if (plan.getUser().getEmail().equals(user.getEmail())) {


            // 상태가 null이면 COMPLETE로 설정
            if (plan.getStatus() == null) {
                plan.setStatus(PlanStatus.COMPLETE);
            } else if (plan.getStatus().equals(PlanStatus.COMPLETE)){
                throw new CustomException(ErrorCode.BAD_REQUEST_400);
            }
            if (plan.getStatus().equals(PlanStatus.DELAYED)) {
                planRepository.deleteById(plan.getChildPlan());
                plan.setDelayToDefault();
            }
            plan.setTime(planTimeRequestDto.startTime, planTimeRequestDto.endTime);
            plan.setStatus(PlanStatus.COMPLETE);
            plan.setComplete(true);

            timeTableRepository.save(TimeTable.builder()
                    .planId(plan.getId())
                    .date(plan.getDate())
                    .startTime(plan.getStartTime())
                    .endTime(plan.getEndTime())
                    .planType(plan.getType())
                    .content(plan.getContent())
                    .isFixed(false)
                    .user(user)
                    .build()
            );
        } else throw new CustomException(ErrorCode.UNAUTHORIZED_401);
    }

    @Transactional
    public void cancelPlan(String email, Long planId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));

        if (plan.getUser().getEmail().equals(user.getEmail())) {
            if (plan.getStatus() == null) {
                plan.setStatus(PlanStatus.CANCELED);
            }
            // 완료된 일정을 취소할 경우
            if(plan.getStatus().equals(PlanStatus.COMPLETE)){
                timeTableRepository.deleteByPlanIdAndIsFixedFalse(planId);
            }
            if (plan.getStatus().equals(PlanStatus.DELAYED)) {
                planRepository.deleteById(plan.getChildPlan());
                plan.setDelayToDefault();
            }
            plan.setStatus(PlanStatus.CANCELED);
            plan.setComplete(false);
        }else{
            throw new CustomException((ErrorCode.UNAUTHORIZED_401));
        }
    }

    @Transactional
    public Long delayPlan(String email, Long planId, DelayPlanRequestDto delayPlanRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));

        if (plan.getUser().getEmail().equals(user.getEmail())) {
            // 일정의 상태가 없을경우
            if(plan.getStatus()==null){
                plan.setStatus(PlanStatus.DELAYED);
                // 완료된 일정을 미룰경우
            } else if (plan.getStatus().equals(PlanStatus.COMPLETE)) {
                timeTableRepository.deleteByPlanIdAndIsFixedFalse(planId);
                // 미룬 일정을 또 미룰경우
            }else if(plan.getStatus().equals(PlanStatus.DELAYED)){
                planRepository.deleteById(plan.getChildPlan());
                plan.setDelayToDefault();
            }
            plan.setStatus(PlanStatus.DELAYED);
            plan.setDelayed(true);
            plan.setComplete(false);
            plan.setCalendar(false);


            Plan childPlan = Plan.builder()
                    .date(delayPlanRequestDto.getDelayedDate())
                    .content(plan.getContent())
                    .priority(plan.getPriority())
                    .type(plan.getType())
                    .user(user)
                    .isCalendar(true)
                    .build();

            planRepository.save(childPlan);

            plan.setChildPlan(childPlan.getId());

            planRepository.save(plan);
            return plan.getChildPlan();
        }
        else{
            throw new CustomException((ErrorCode.UNAUTHORIZED_401));
        }
    }


    @Transactional
    public void updateAddedTime(String email, Long planId, PlanTimeRequestDto planTimeRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));

        if (plan.getUser().getEmail().equals(user.getEmail())) {
            plan.setTime(planTimeRequestDto.startTime, planTimeRequestDto.endTime);
        }
        else{
            throw new CustomException((ErrorCode.UNAUTHORIZED_401));
        }
    }

    @Transactional
    public void updateFixedTime(String email, Long fixedPlanId, PlanTimeRequestDto planTimeRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        FixedPlan fixedPlan = fixedPlanRepository.findById(fixedPlanId).orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));


        if(fixedPlan.getUser().getEmail().equals(user.getEmail())){
            LocalDate date = LocalDate.now();    // 지금 기준 변경

            LocalTime newStartTime = planTimeRequestDto.getStartTime();
            LocalTime newEndTime = planTimeRequestDto.getEndTime();

            fixedPlan.updateTime(newStartTime, newEndTime);
        }
        else{
            throw new CustomException((ErrorCode.UNAUTHORIZED_401));
        }
    }

    public List<PlanResponseDto> getPlans(String email, LocalDate date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));
        List<Plan> plans = planRepository.findByDateAndUser_Email(date, email);
        return PlanResponseDto.from(plans);
    }


    @Transactional
    public TimeTableResponseDto getTimeTable(String email, LocalDate date) {
        boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_PLAN_DATA_REGISTERED));


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
                .orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        LocalDate startDate = calendarPlanRequestDto.getStartDate();
        LocalDate endDate = calendarPlanRequestDto.getEndDate();

        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
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
                    .isCalendar(true)  // 캘린더화면에서 생성한 Plan
                    .build();

            plans.add(plan);

            startDate = startDate.plusDays(1);
        }
        planRepository.saveAll(plans);

        return PlanResponseDto.from(plans);
    }

    @Transactional
    public void finish(String email, FinishRequestDto finishRequestDto){
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
                    .content(fixedPlan.getContent())
                    .isFixed(true)
                    .planId(fixedPlan.getId()) // FixedPlan의 ID를 저장
                    .user(fixedPlan.getUser())
                    .build();
            timeTables.add(timeTable);
        }

        timeTableRepository.saveAll(timeTables);
    }


    public List<PlanResponseDto> getCalendarPlans(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.NO_USER_DATA_REGISTERED));

        List<Plan> plans = planRepository.findByUserEmailIsCalendarTrue(email);

        return PlanResponseDto.from(plans);
    }

}
