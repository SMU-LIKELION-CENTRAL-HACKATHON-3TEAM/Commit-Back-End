package com.likelion.commit.repository;

import com.likelion.commit.entity.RuleSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface RuleSetRepository extends JpaRepository<RuleSet, Long> {

    Optional<RuleSet> findByUser_Id(Long userId);

}
