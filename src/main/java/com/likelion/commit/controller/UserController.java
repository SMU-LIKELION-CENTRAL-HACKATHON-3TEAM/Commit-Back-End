package com.likelion.commit.controller;

import com.likelion.commit.Security.dto.LoginRequestDto;
import com.likelion.commit.dto.request.CreateRuleSetRequestDto;
import com.likelion.commit.dto.request.CreateUserRequestDto;
import com.likelion.commit.dto.request.UpdatePasswordRequestDto;
import com.likelion.commit.dto.request.UpdateRuleSetRequestDto;
import com.likelion.commit.dto.response.RuleSetResponseDto;
import com.likelion.commit.dto.response.UserResponseDto;
import com.likelion.commit.gloabal.response.ApiResponse;
import com.likelion.commit.gloabal.response.ErrorCode;
import com.likelion.commit.service.RuleSetService;
import com.likelion.commit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RuleSetService ruleSetService;

    @PostMapping("/create")
    public ApiResponse<Map<String, Long>> createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {
        try {
            long userId = userService.createUser(createUserRequestDto);

            // 결과 데이터 생성
            Map<String, Long> result = new HashMap<>();
            result.put("userId", userId);

            return ApiResponse.onSuccess(HttpStatus.CREATED, result);
        } catch (Exception e) {
            return ApiResponse.onFailure(
                    ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(),
                    "서버에서 오류가 발생했습니다."
            );
        }
    }

    @PutMapping("/updatePassword")
    public ApiResponse<String> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto) {
        try {
            userService.updatePassword(userDetails.getUsername(), updatePasswordRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, "비밀번호 변경이 완료되었습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.deleteUser(userDetails.getUsername());
            return ApiResponse.onSuccess(HttpStatus.NO_CONTENT, "회원 탈퇴가 완료되었습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @GetMapping("")
    public ApiResponse<UserResponseDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserResponseDto user = userService.getUser(userDetails.getUsername());
            return ApiResponse.onSuccess(HttpStatus.OK, user);
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    // RuleSet 부분

    @PostMapping("/ruleSet/create")
    public ApiResponse<Map<String, Long>> createRuleSet(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody CreateRuleSetRequestDto createRuleSetRequestDto) {
        try {
            long ruleSetId = ruleSetService.createRuleSet(userDetails.getUsername(), createRuleSetRequestDto);

            Map<String, Long> result = new HashMap<>();
            result.put("ruleSetId", ruleSetId);

            return ApiResponse.onSuccess(HttpStatus.CREATED, result);
        } catch (Exception e) {
            return ApiResponse.onFailure(
                    ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(),
                    "서버에서 오류가 발생했습니다."
            );
        }
    }


    @GetMapping("/ruleSet")
    public ApiResponse<RuleSetResponseDto> getRuleSet(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            RuleSetResponseDto ruleSet = ruleSetService.getRuleSet(userDetails.getUsername());
            return ApiResponse.onSuccess(HttpStatus.OK, ruleSet);
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "규칙을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }

    @PutMapping("/ruleSet/update")
    public ApiResponse<String> updateRuleSet(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody UpdateRuleSetRequestDto updateRuleSetRequestDto) {
        try {
            ruleSetService.updateRuleSet(userDetails.getUsername(), updateRuleSetRequestDto);
            return ApiResponse.onSuccess(HttpStatus.OK, "커스텀 규칙이 수정되었습니다.");
        } catch (NoSuchElementException e) {
            return ApiResponse.onFailure(ErrorCode.NO_USER_DATA_REGISTERED.getCode(), "규칙을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR_500.getCode(), "서버에서 오류가 발생했습니다.");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDTO){
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }
}



