package com.likelion.commit.service;


import com.likelion.commit.dto.request.CreateRuleSetRequestDto;
import com.likelion.commit.dto.response.RuleSetResponseDto;
import com.likelion.commit.dto.request.UpdateRuleSetRequestDto;
import com.likelion.commit.entity.RuleSet;
import com.likelion.commit.entity.User;
import com.likelion.commit.repository.RuleSetRepository;
import com.likelion.commit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleSetService {

    private final RuleSetRepository ruleSetRepository;
    private final UserRepository userRepository;


    @Transactional
    public long createRuleSet(String email, CreateRuleSetRequestDto createRuleSetRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        RuleSet ruleSet = createRuleSetRequestDto.toEntity();
        ruleSet.setUser(user);
        user.setRuleSet(ruleSet);

        ruleSetRepository.save(ruleSet);

        return ruleSet.getId();
    }

    public RuleSetResponseDto getRuleSet(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        return RuleSetResponseDto.from(user.getRuleSet());
    }
    @Transactional
    public void updateRuleSet(String email, UpdateRuleSetRequestDto updateRuleSetRequestDto){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        RuleSet ruleSet = user.getRuleSet();
        ruleSet.update(updateRuleSetRequestDto);

        ruleSetRepository.save(ruleSet);

    }
}
