package com.likelion.commit.Security.Controller;

import com.likelion.commit.Security.dto.JwtDto;
import com.likelion.commit.Security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*" ,value = "*")
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    //토큰 재발급 API
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody JwtDto jwtDto) {

        log.info("[ Auth Controller ] 토큰을 재발급합니다. ");

        return ResponseEntity.ok(authService.reissueToken(jwtDto));
    }
}