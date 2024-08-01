package com.likelion.commit.repository;

import com.likelion.commit.entity.Diary;
import com.likelion.commit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Diary findByUserAndDate(User user, LocalDate date);
}
