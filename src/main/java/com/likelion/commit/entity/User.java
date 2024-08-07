package com.likelion.commit.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String password;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    private AuthType authType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ruleSetId", referencedColumnName = "id")
    private RuleSet ruleSet;

    private String Role;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan> plans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedPlan> fixedPlans;

    public void setPassword(String newPassword) {
        password = newPassword;
    }

    public void setRuleSet(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }


    // 기타 맵핑 부분 추가

}
