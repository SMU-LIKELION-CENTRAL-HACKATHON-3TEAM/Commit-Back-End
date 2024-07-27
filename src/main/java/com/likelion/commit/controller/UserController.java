package com.likelion.commit.controller;

import com.likelion.commit.dto.CreateRuleSetRequestDto;
import com.likelion.commit.dto.CreateUserRequestDto;
import com.likelion.commit.dto.UpdatePasswordRequestDto;
import com.likelion.commit.dto.UpdateRuleSetRequestDto;
import com.likelion.commit.service.RuleSetService;
import com.likelion.commit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RuleSetService ruleSetService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequestDto createUserRequestDto){
        long userId = userService.createUser(createUserRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다. userId: " + userId);
    }

    // 파라미터에서 String email은 인가적용 시 교체
    @PutMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestParam("email") String email, @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto){
        userService.updatePassword(email, updatePasswordRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("비밀번호 변경이 완료되었습니다.");
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam("email") String email){
        userService.deleteUser(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("회원 탈퇴가 완료되었습니다.");
    }

    @GetMapping("")
    public ResponseEntity<?> getUser(@RequestParam("email") String email){
        return ResponseEntity.ok(userService.getUser(email));
    }




    // RuleSet 부분

    @PostMapping("/ruleSet/create")
    public ResponseEntity<?> createRuleSet(@RequestParam("email") String email,
                                           @RequestBody CreateRuleSetRequestDto createRuleSetRequestDto){
        long ruleSetId = ruleSetService.createRuleSet(email, createRuleSetRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("커스텀 규칙이 생성되었습니다. 규칙id: " + ruleSetId);
    }



    @GetMapping("/ruleSet")
    public ResponseEntity<?> getRuleSet(@RequestParam("email") String email){
        return ResponseEntity.ok(ruleSetService.getRuleSet(email));
    }

    @PutMapping("/ruleSet/update")
    public ResponseEntity<?> updateRuleSet(@RequestParam("email") String email,
                                           @RequestBody UpdateRuleSetRequestDto updateRuleSetRequestDto){
        ruleSetService.updateRuleSet(email, updateRuleSetRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("커스텀 규칙이 수정되었습니다.");
    }


}


