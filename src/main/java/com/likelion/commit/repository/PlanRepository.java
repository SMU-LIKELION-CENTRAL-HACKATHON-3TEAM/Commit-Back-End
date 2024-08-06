package com.likelion.commit.repository;

import com.likelion.commit.entity.Plan;
import com.likelion.commit.entity.PlanStatus;
import com.likelion.commit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findById(Long id);
    List<Plan> findByDateAndUser_Email(LocalDate date, String email);
    @Query("SELECT p FROM Plan p WHERE p.date > :currentDate AND p.user.email = :email ORDER BY p.date ASC")
    List<Plan> findUpcomingPlans(@Param("currentDate") LocalDate currentDate, @Param("email") String email);
    @Query("SELECT p FROM Plan p WHERE p.user.email = :email AND p.isCalendar = true ORDER BY p.date ASC")
    List<Plan> findByUserEmailIsCalendarTrue(String email);
}
