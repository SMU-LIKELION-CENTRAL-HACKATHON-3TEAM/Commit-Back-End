package com.likelion.commit.repository;

import com.likelion.commit.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

    List<TimeTable> findByUser_IdAndDate(Long userId, LocalDate localDate);
}
