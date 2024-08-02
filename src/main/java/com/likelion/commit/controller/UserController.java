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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "유저 API", description = "유저 관련 API입니다.")
public class UserController {

    private final UserService userService;
    private final RuleSetService ruleSetService;

    @Operation(method = "POST",
            summary = "회원가입",
            description = "회원가입API입니다. CreateUserRequestDto 형태로 RequestBody에 담아서 요청합니다.")
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

    @Operation(method = "PUT",
            summary = "비밀번호 변경",
            description = "로그인한 유저의 비밀번호를 변경합니다. header에 accessToken과 body에 UpdatePasswordRequestDto 형태로 담아서 요청합니다. ")
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

    @Operation(method = "DELETE",
            summary = "회원 탈퇴",
            description = "로그인한 유저를 삭제시킵니다. header에 accessToken을 담아 요청합니다.")
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

    @Operation(method = "GET",
            summary = "회원 조회",
            description = "로그인한 유저의 정보를 조회합니다.  header에 accessToken을 담아 요청하면 UserResponseDto형태로 반환합니다.")
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


    @Operation(method = "POST",
            summary = "플래너 작성 규칙 생성",
            description = "사용자 설정에서 나만의 플래너 규칙을 생성합니다. header에 accessToken과 body에 CreateRuleSetRequestDto형태로 담아 요청합니다.")
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


    @Operation(method = "GET",
            summary = "플래너 작성 규칙 조회",
            description = "사용자 설정에서 나만의 플래너 규칙을 조회합니다. header에 accessToken을 담아서 요청하면 RuleSetResponseDto형태로 반환합니다.")
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
    @Operation(method = "PUT",
            summary = "플래너 작성 규칙 수정",
            description = "사용자 설정에서 나만의 플래너 규칙을 수정합니다.  header에 accessToken과 body에 UpdateRuleSetRequestDto형태로 담아 요청합니다.")
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

    @Operation(method = "POST",
            summary = "로그인",
            description = "로그인합니다. email과 password를 body에 담아서 전송합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDTO){
        return null;
    }

    @Operation(method = "POST", summary = "로그아웃", description = "로그아웃합니다. accessToken을 header에 담아서 전송합니다. ")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }
}



