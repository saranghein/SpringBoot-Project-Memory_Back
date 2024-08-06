package com.memory.meco;

import com.memory.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "meco")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mecoId;

    // User 개발 후
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

//    @Column(name = "userId", nullable = false)
//    private String userId;

    @Column(nullable = true)
    private LocalDate mecoDate;

    @Column(nullable = true)
    private String contents;

    @Convert(converter = ListToStringConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> questions;

    @Convert(converter = ListToStringConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> answers;

}
