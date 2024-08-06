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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
@Tag(name = "일정 API", description = "일정 관련 API입니다.")
public class PlanController {

    private final PlanService planService;
    private final DiaryService diaryService;
    @Operation(method = "POST",
            summary = "일정 생성",
            description = "일정을 생성합니다. header에 accessToken과 body에 List<CreatePlanRequestDto>형태로 담아 요청하면 List<PlanResponseDto>형태로 반환합니다.")
    @PostMapping("/create")
    public ApiResponse<List<PlanResponseDto>> createPlans(@AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody List<CreatePlanRequestDto> createPlanRequestDtoList) {

        List<PlanResponseDto> responseDtos = planService.createPlans(userDetails.getUsername(), createPlanRequestDtoList);
        return ApiResponse.onSuccess(HttpStatus.CREATED, "일정 추가에 성공했습니다.", responseDtos);
    }

    @Operation(method = "PUT",
            summary = "일정 수정",
            description = "일정을 수정합니다. header에 accessToken과 body에 List<UpdatePlanRequestDto>형태로 담아 요청합니다.")
    @PutMapping("/update")
    public ApiResponse<String> updatePlan(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody List<UpdatePlanRequestDto> updatePlanRequestDtos) {
        planService.updatePlans(userDetails.getUsername(), updatePlanRequestDtos);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정 수정에 설정했습니다.", null);
    }

    @Operation(method = "DELETE",
            summary = "일정 삭제",
            description = "일정을 삭제합니다. header에 accessToken과 body에 List<DeletePlanRequestDto>형태로 담아 요청합니다.")
    @DeleteMapping("/delete")
    public ApiResponse<String> deletePlan(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody List<DeletePlanRequestDto> deletePlanRequestDtos) {
        planService.deletePlans(userDetails.getUsername(), deletePlanRequestDtos);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정 삭제 완료했습니다.", null);
    }
    @Operation(method = "POST",
            summary = "일정 완료",
            description = "메인화면에서 일정을 완료합니다. header에 accessToken과 url parameter에 일정id를 보내고 body에는 PlanTimeRequestDto형태로 담아 요청합니다.")
    @PostMapping("/complete/{planId}")
    public ApiResponse<String> completePlan(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long planId,
                                            @RequestBody PlanTimeRequestDto planTimeRequestDto) {

        planService.completePlan(userDetails.getUsername(), planId, planTimeRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정 완료 했습니다.", null);
    }
    @Operation(method = "POST",
            summary = "일정 취소",
            description = "메인화면에서 일정을 취소합니다. header에 accessToken과 url parameter에 일정id 담아 요청합니다.")
    @PostMapping("/cancel/{planId}")
    public ApiResponse<String> cancelPlan(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long planId) {

        planService.cancelPlan(userDetails.getUsername(), planId);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정을 취소 했습니다.", null);
    }

    @Operation(method = "POST",
            summary = "일정 미루기",
            description = "메인화면에서 일정을 미룹니다. header에 accessToken과 url parameter에 일정id를 보내고 body에는 PlanTimeRequestDto형태로 담아 요청합니다.")
    @PostMapping("/delay/{planId}")
    public ApiResponse<Map<String, Long>> delayPlan(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long planId,
                                         @RequestBody DelayPlanRequestDto delayPlanRequestDto) {
        Long childPlanId = planService.delayPlan(userDetails.getUsername(), planId, delayPlanRequestDto);
        // 결과를 Map으로 래핑
        Map<String, Long> result = new HashMap<>();
        result.put("childPlanId", childPlanId);

        return ApiResponse.onSuccess(HttpStatus.OK, "일정 미루기를 완료 했습니다.", result);
    }

    @Operation(method = "PUT",
            summary = "커스텀 일정 수정",
            description = "커스텀 일정을 수정합니다. header에 accessToken과 url parameter에 일정id를 보내고 body에는 PlanTimeRequestDto형태로 담아 요청합니다.")
    @PutMapping("/time/{planId}")
    public ApiResponse<String> updateAddedTime(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long planId,
                                               @RequestBody PlanTimeRequestDto planTimeRequestDto) {
        planService.updateAddedTime(userDetails.getUsername(), planId, planTimeRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정의 시간을 수정했습니다.", null);
    }

    @Operation(method = "PUT",
            summary = "고정 일정 수정",
            description = "고정 일정을 수정합니다. header에 accessToken과 url parameter에 고정일정id를 보내고 body에는 PlanTimeRequestDto형태로 담아 요청합니다. 요청을 보낸시간 기준으로 전에는 변경 전 후에는 변경 후의 시간이 적용됩니다.")
    @PutMapping("/timetable/time/{fixedPlanId}")
    public ApiResponse<String> updateFixedTime(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long fixedPlanId,
                                               @RequestBody PlanTimeRequestDto planTimeRequestDto) {
        planService.updateFixedTime(userDetails.getUsername(), fixedPlanId, planTimeRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정의 시간을 수정했습니다.", null);
    }

    @Operation(method = "GET",
            summary = "날짜별 일정 조회",
            description = "날짜별 일정을 조회합니다. header에 accessToken과 파라미테어 date를 담아 요청하면 List<PlanResponseDto> 형태로 반환합니다.")
    @GetMapping("/date")
    public ApiResponse<List<PlanResponseDto>> getPlan(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("date") String date) {
        LocalDate d = LocalDate.parse(date);
        List<PlanResponseDto> plans = planService.getPlans(userDetails.getUsername(), d);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정 조회에 성공했습니다", plans);
    }

    @Operation(method = "GET",
            summary = "날짜별 타임 테이블 조회",
            description = "날짜별 타임 테이블을 조회합니다. header에 accessToken과 파라미테어 date를 담아 요청합니다. 날짜별 일정 중 완료된 것들과 고정일정들이 TimeTableResponseDto 형태로 반환됩니다.")
    @GetMapping("/timetable")
    public ApiResponse<TimeTableResponseDto> getTimeTable(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("date") String date) {
        LocalDate d = LocalDate.parse(date);
        TimeTableResponseDto timeTable = planService.getTimeTable(userDetails.getUsername(), d);
        return ApiResponse.onSuccess(HttpStatus.OK, "타임테이블 조회에 성공했습니다",timeTable);
    }

    @Operation(method = "GET",
            summary = "앞으로의 일정 조회",
            description = "요청을 보내는 시점을 기준으로 다음날부터 최대 3가지의 가까운 일정들을 조회합니다. header에 accessToken을 담아 요청하면 List<PlanResponseDto>형태로 반환합니다.")
    @GetMapping("/upcoming")
    public ApiResponse<List<PlanResponseDto>> getUpcomingPlans(@AuthenticationPrincipal UserDetails userDetails) {

        List<PlanResponseDto> upcomingPlans = planService.getUpcomingPlans(userDetails.getUsername());
        return ApiResponse.onSuccess(HttpStatus.OK, "앞으로의 일정 조회에 성공했습니다",upcomingPlans);
    }
    @Operation(method = "POST",
            summary = "캘린더 일정 추가",
            description = "캘린더 화면에서 일정을 추가합니다. header에 accessToken과 body에 CalendarPlanRequestDto형태로 담아 요청하면 List<PlanResponseDto>형태로 반환합니다.")
    @PostMapping("/calendar/create")
    public ApiResponse<List<PlanResponseDto>> createCalendarPlans(@AuthenticationPrincipal UserDetails userDetails,
                                                                  @RequestBody CalendarPlanRequestDto calendarPlanRequestDto){

        List<PlanResponseDto> planResponseDtos = planService.createCalendarPlans(userDetails.getUsername(), calendarPlanRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "일정 조회에 성공했습니다",planResponseDtos);
    }


    @Operation(method = "POST",
            summary = "오늘 하루 마무리 하기",
            description = "오늘 하루 마무리화면에서 이날의 기록사항과 해당일의 타임테이블을 저장합니다. header에 accessToken과 body에 FinishRequestDto형태로 담아 요청합니다.")
    @PostMapping("finish")
    public ApiResponse<String> finish(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody FinishRequestDto finishRequestDto) {
         planService.finish(userDetails.getUsername(), finishRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "오늘 하루 마무리하기에 성공했습니다.");
    }

    @Operation(method = "GET",
            summary = "이날의 기록사항 조회",
            description = "이날의 기록사항을 조회합니다. header에 accessToken과 파라미터에 date를 담아 요청하면 DiaryResponseDto형태로 반환합니다.")
    @GetMapping("/diary")
    public ApiResponse<DiaryResponseDto> getDiary(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam("date") String date){
        LocalDate d = LocalDate.parse(date);
        DiaryResponseDto diaryResponseDto = diaryService.getDiary(userDetails.getUsername(), d);
        return ApiResponse.onSuccess(HttpStatus.OK, "기록 사항 조회에 성공했습니다.", diaryResponseDto);
    }

    @Operation(method = "GET",
            summary = "월별 일정 조회",
            description = "캘린더 화면에서 월별 일정들을 조회합니다. header에 accessToken과 파라미테어 yearMonth를 담아 요청하면 List<PlanResponseDto>형태로 반환합니다.")
    @GetMapping("/month")
    public ApiResponse<List<PlanResponseDto>> getMonthlyPlans(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("yearMonth") String yearMonth) {

        YearMonth ym = YearMonth.parse(yearMonth);
        List<PlanResponseDto> planResponseDtos = planService.getMonthlyPlans(userDetails.getUsername(), ym);
        return ApiResponse.onSuccess(HttpStatus.OK, "월별 일정 조회에 성공했습니다.", planResponseDtos);
    }


    @Operation(method = "POST",
            summary = "이날의 기록사항 저장",
            description = "이날의 기록사항을 저장합니다. header에 accessToken과 body에 CreateDiaryRequestDto형태로 담아 요청하면 diaryId를 반환합니다.")
    @PostMapping("/diary")
    public ApiResponse<Map<String, Long>> createDiary(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody CreateDiaryRequestDto createDiaryRequestDto){
        Long diaryId = diaryService.createDiary(userDetails.getUsername(), createDiaryRequestDto);

        // 결과 데이터 생성
        Map<String, Long> result = new HashMap<>();
        result.put("diaryId", diaryId);

        return ApiResponse.onSuccess(HttpStatus.CREATED, "기록사항 저장에 성공했습니다.", result);
    }

    @Operation(method = "PUT",
            summary = "이날의 기록사항 수정",
            description = "이날의 기록사항을 수정합니다. header에 accessToken과 body에 UpdateDiaryRequestDto형태로 담아 요청합니다.")
    @PutMapping("/diary")
    public ApiResponse<String> updateDiary(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody UpdateDiaryRequestDto updateDiaryRequestDto){
        diaryService.updateDiary(userDetails.getUsername(), updateDiaryRequestDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "기록사항 수정에 성공했습니다.");
    }

}

