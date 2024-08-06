package com.likelion.commit.entity;


import com.likelion.commit.dto.request.UpdateDiaryRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "diary")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;


    public void setUser(User user) {
        this.user = user;
    }

    public void update(UpdateDiaryRequestDto updateDiaryRequestDto){
        this.content = updateDiaryRequestDto.getContent();
    }
}
