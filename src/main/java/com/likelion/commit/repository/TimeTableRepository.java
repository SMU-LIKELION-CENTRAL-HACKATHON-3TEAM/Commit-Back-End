package com.likelion.commit.repository;

import com.likelion.commit.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

    List<TimeTable> findByUser_IdAndDate(Long userId, LocalDate localDate);
    @Modifying
    @Query("DELETE FROM TimeTable t WHERE t.planId = :planId AND t.isFixed = false")
    void deleteByPlanIdAndIsFixedFalse(@Param("planId") Long planId);
    @Modifying
    @Query("SELECT t FROM TimeTable t WHERE t.planId = :planId AND t.isFixed = false")
    Optional<TimeTable> findByPlanIdAndIsFixedFalse(@Param("planId") Long planId);
}
