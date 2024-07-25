package com.memory.ledger;

import com.memory.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ledger {

    /*
    id : Long
    emotion : String
    emotionCategory : String
    category : String
    contents : String
    takedTime : float
    userId : String
    ledgerDate : LocalDateTime
     */

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long recordId;

        @Column(nullable = false)
        private String emotion;

        @Column(nullable = false)
        private String emotionCategory;

        @Column(nullable = false)
        private String category;

        @Column(nullable = false)
        private String contents;

        @Column(nullable = false)
        private Float takedTime;

        //User 개발 후
        @ManyToOne
        @JoinColumn(name = "userId", nullable = false)
        private User user;

//        @Column(name = "userId", nullable = false)
//        private String userId;

        @Column(nullable = false)
        private LocalDate ledgerDate;
}
