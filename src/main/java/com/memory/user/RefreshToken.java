package com.memory.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RefreshToken {
    @Id
    @Column(nullable = false)
    private String refreshToken;

}
