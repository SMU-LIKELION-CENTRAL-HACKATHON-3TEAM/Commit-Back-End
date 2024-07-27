package com.likelion.commit.repository;

import com.likelion.commit.entity.RuleSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSet, Long> {

}
