package com.likelion.commit.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
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


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ruleSetId", referencedColumnName = "id")
    private RuleSet ruleSet;


    // 기타 맵핑 부분 추가

}
