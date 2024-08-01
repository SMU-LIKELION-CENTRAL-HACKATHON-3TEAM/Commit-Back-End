package com.likelion.commit.repository;

import com.likelion.commit.entity.FixedPlan;
import com.likelion.commit.entity.Plan;
import com.likelion.commit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FixedPlanRepository extends JpaRepository<FixedPlan, Long> {
    List<FixedPlan> findByDateAndUser_Email(LocalDate date, String email);

    List<FixedPlan> findByUserAndDateGreaterThan(User user, LocalDate date);
}
