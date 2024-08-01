package com.likelion.commit.service;


import com.likelion.commit.dto.request.*;
import com.likelion.commit.dto.response.PlanResponseDto;
import com.likelion.commit.dto.response.TimeTableResponseDto;
import com.likelion.commit.entity.FixedPlan;
import com.likelion.commit.entity.Plan;
import com.likelion.commit.entity.PlanStatus;
import com.likelion.commit.entity.User;
import com.likelion.commit.repository.FixedPlanRepository;
import com.likelion.commit.repository.PlanRepository;
import com.likelion.commit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
        }
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

//    @Transactional
//    public void updateFixedTime(String email, Long fixedPlanId, PlanTimeRequestDto planTimeRequestDto) {
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
//        FixedPlan existingFixedPlan = fixedPlanRepository.findById(fixedPlanId).orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없습니다."));
//
//        LocalDate changeDate = LocalDate.now();    // 지금 기준 변경
//        LocalDateTime newStartTime = planTimeRequestDto.getStartTime();
//        LocalDateTime newEndTime = planTimeRequestDto.getEndTime();
//
//
//        // FixedPlan의 날짜가 변경된 날짜와 동일하거나 이후인 경우에만 업데이트
//        if (existingFixedPlan.getDate().isAfter(changeDate.minusDays(1))) {
//            // 변경된 날짜 이후의 FixedPlan들을 조회
//            List<FixedPlan> fixedPlansToUpdate = fixedPlanRepository.findByUserAndDateGreaterThan(user, changeDate.minusDays(1));
//
//            for (FixedPlan fixedPlan : fixedPlansToUpdate) {
//                // 기존 FixedPlan의 날짜가 변경된 날짜와 동일하거나 이후인 경우에만 업데이트
//                if (fixedPlan.getDate().isEqual(changeDate) || fixedPlan.getDate().isAfter(changeDate)) {
//                    fixedPlan.setStartTime(newStartTime);
//                    fixedPlan.setEndTime(newEndTime);
//                    fixedPlanRepository.save(fixedPlan);
//                }
//            }
//        }
//        // 변경된 FixedPlan의 시작과 끝 시간을 업데이트
//        existingFixedPlan.setStartTime(newStartTime);
//        existingFixedPlan.setEndTime(newEndTime);
//        fixedPlanRepository.save(existingFixedPlan);
//
//    }

    public List<PlanResponseDto> getPlans(String email, PlanDateRequestDto planDateRequestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        List<Plan> plans = planRepository.findByDateAndUser_Email(planDateRequestDto.getDate(), email);
        return PlanResponseDto.from(plans);
    }


    @Transactional
    public List<TimeTableResponseDto> getTimeTable(String email, PlanDateRequestDto planDateRequestDto) {
        LocalDate date = planDateRequestDto.getDate();

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // FixedPlan 조회
        List<FixedPlan> fixedPlans = fixedPlanRepository.findByDateAndUser_Email(date, email);


        // 로그 추가
        System.out.println("FixedPlans: " + fixedPlans);

        // 모든 Plan을 조회
        List<Plan> plans = planRepository.findByDateAndUser_Email(date, email);

        // 로그 추가
        System.out.println("Plans: " + plans);

        // 요일을 판단하여 주말인지 평일인지 결정합니다.
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        // FixedPlan을 필터링하고 DTO로 변환합니다.
        List<TimeTableResponseDto> fixedPlanDtos = fixedPlans.stream()
                .filter(fp -> fp.isWeekend() == isWeekend)
                .map(fp -> TimeTableResponseDto.builder()
                        .fixedPlanId(fp.getId())
                        .startTime(fp.getStartTime())
                        .endTime(fp.getEndTime())
                        .isWeekend(fp.isWeekend())
                        .content(fp.getContent())
                        .userId(fp.getUser().getId())
                        .isFixed(true)
                        .priority(0)
                        .build())
                .collect(Collectors.toList());

        // Plan을 필터링하고 DTO로 변환합니다.
        List<TimeTableResponseDto> planDtos = plans.stream()
                .filter(p -> p.getStatus() == PlanStatus.COMPLETE && p.getStartTime() != null && p.getEndTime() != null)
                .map(p -> TimeTableResponseDto.builder()
                        .planId(p.getId())
                        .startTime(p.getStartTime())
                        .endTime(p.getEndTime())
                        .isWeekend(false)
                        .content(p.getContent())
                        .userId(p.getUser().getId())
                        .isFixed(false)
                        .priority(p.getPriority())
                        .build())
                .collect(Collectors.toList());

        // 두 리스트를 결합합니다.
        List<TimeTableResponseDto> allDtos = new ArrayList<>();
        allDtos.addAll(fixedPlanDtos);
        allDtos.addAll(planDtos);

        // 시작 시간 기준으로 정렬합니다.
        return allDtos.stream()
                .sorted(Comparator.comparing(TimeTableResponseDto::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
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


}
