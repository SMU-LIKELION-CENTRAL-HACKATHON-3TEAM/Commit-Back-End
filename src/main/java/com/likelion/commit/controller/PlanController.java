package com.likelion.commit.controller;


import com.likelion.commit.dto.request.*;
import com.likelion.commit.dto.response.DiaryResponseDto;
import com.likelion.commit.dto.response.PlanResponseDto;
import com.likelion.commit.dto.response.TimeTableResponseDto;
import com.likelion.commit.dto.response.UserResponseDto;
import com.likelion.commit.entity.Plan;
import com.likelion.commit.gloabal.response.ApiResponse;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.service.DiaryService;
import com.likelion.commit.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;
    private final DiaryService diaryService;

    @PostMapping("/create")
    public ApiResponse<List<PlanResponseDto>> createPlans(@RequestParam String email,
                                                          @RequestBody List<CreatePlanRequestDto> createPlanRequestDtoList) {
        try {
            List<PlanResponseDto> responseDtos = planService.createPlans(email, createPlanRequestDtoList);
            return ApiResponse.onSuccess(HttpStatus.CREATED, responseDtos);
        } catch (Exception e) {
            log.error("Error creating plans: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PutMapping("/update")
    public ApiResponse<String> updatePlan(@RequestParam String email,
                                          @RequestBody List<UpdatePlanRequestDto> updatePlanRequestDtos) {
        try {
            planService.updatePlans(email, updatePlanRequestDtos);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정 수정에 설정했습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error updating plans: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> deletePlan(@RequestParam String email,
                                          @RequestBody List<DeletePlanRequestDto> deletePlanRequestDtos) {
        try {
            planService.deletePlans(email, deletePlanRequestDtos);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정 삭제 완료했습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error deleting plans: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PostMapping("/complete/{planId}")
    public ApiResponse<String> completePlan(@RequestParam String email, @PathVariable Long planId,
                                            @RequestBody PlanTimeRequestDto planTimeRequestDto) {
        try {
            planService.completePlan(email, planId, planTimeRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정 완료 했습니다. planId :" + planId);
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error completing plan: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PostMapping("/cancel/{planId}")
    public ApiResponse<String> cancelPlan(@RequestParam String email, @PathVariable Long planId) {
        try {
            planService.cancelPlan(email, planId);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정을 취소 했습니다. planId :" + planId);
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error canceling plan: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PostMapping("/delay/{planId}")
    public ApiResponse<String> delayPlan(@RequestParam String email, @PathVariable Long planId,
                                         @RequestBody DelayPlanRequestDto delayPlanRequestDto) {
        try {
            planService.delayPlan(email, planId, delayPlanRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정 미루기를 완료 했습니다. planId :" + planId);
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error delaying plan: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PutMapping("/time/{planId}")
    public ApiResponse<String> updateAddedTime(@RequestParam String email, @PathVariable Long planId,
                                               @RequestBody PlanTimeRequestDto planTimeRequestDto) {
        try {
            planService.updateAddedTime(email, planId, planTimeRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정의 시간을 수정했습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error updating added time: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PutMapping("/timetable/time/{fixedPlanId}")
    public ApiResponse<String> updateFixedTime(@RequestParam String email, @PathVariable Long fixedPlanId,
                                               @RequestBody PlanTimeRequestDto planTimeRequestDto) {
        try {
            planService.updateFixedTime(email, fixedPlanId, planTimeRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, "일정의 시간을 수정했습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "일정을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Error updating fixed time: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @GetMapping("/date")
    public ApiResponse<List<PlanResponseDto>> getPlan(@RequestParam String email, @RequestBody PlanDateRequestDto planDateRequestDto) {
        try {
            List<PlanResponseDto> plans = planService.getPlans(email, planDateRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, plans);
        } catch (Exception e) {
            log.error("Error retrieving plans: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @GetMapping("/timetable")
    public ApiResponse<TimeTableResponseDto> getTimeTable(@RequestParam String email, @RequestBody PlanDateRequestDto planDateRequestDto) {
        try {
            TimeTableResponseDto timeTable = planService.getTimeTable(email, planDateRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, timeTable);
        } catch (Exception e) {
            log.error("Error retrieving timetable: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<PlanResponseDto>> getUpcomingPlans(@RequestParam String email) {
        try {
            List<PlanResponseDto> upcomingPlans = planService.getUpcomingPlans(email);
            return ApiResponse.onSuccess(HttpStatus.OK, upcomingPlans);
        } catch (Exception e) {
            log.error("Error retrieving upcoming plans: ", e);
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PostMapping("/calendar/create")
    public ApiResponse<List<PlanResponseDto>> createCalendarPlans(@RequestParam String email,
                                                                  @RequestBody CalendarPlanRequestDto calendarPlanRequestDto){
        try {
            List<PlanResponseDto> planResponseDtos = planService.createCalendarPlans(email, calendarPlanRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, planResponseDtos);
        } catch (IllegalArgumentException e) {
            return ApiResponse.onFailure(String.valueOf(HttpStatus.BAD_REQUEST.value()), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.onFailure(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "서버에서 오류가 발생했습니다.");
        }
    }


    @PostMapping("finish")
    public ApiResponse<String> finish(@RequestParam String email,
                                      @RequestBody FinishRequestDto finishRequestDto){
        Long diaryId = planService.finish(email, finishRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "하루 마무리하기를 완료했습니다. diaryId :" + diaryId);
    }

    @GetMapping("/diary")
    public ApiResponse<DiaryResponseDto> getDiary(@RequestParam String email,
                                                  @RequestBody DiaryDateRequestDto diaryDateRequestDto){
        try {
            DiaryResponseDto diaryResponseDto = diaryService.getDiary(email, diaryDateRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, diaryResponseDto);
        }catch (Exception e) {
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @GetMapping("/month")
    public ApiResponse<List<PlanResponseDto>> getMonthlyPlans(@RequestParam String email, @RequestBody MonthPlanRequestDto monthPlanRequestDto) {
        try {
            YearMonth ym = YearMonth.parse(monthPlanRequestDto.getYearMonth());
            List<PlanResponseDto> planResponseDtos = planService.getMonthlyPlans(email, ym);
            return ApiResponse.onSuccess(HttpStatus.OK, planResponseDtos);
        }catch (Exception e) {
            return ApiResponse.onFailure(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "서버에서 오류가 발생했습니다.");
        }
    }


}

