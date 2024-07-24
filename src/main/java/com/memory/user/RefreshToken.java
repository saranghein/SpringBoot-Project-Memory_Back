package com.memory.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "refreshToken")
@Data
public class RefreshToken {
    @Id
    @Column(nullable = false)
    private String refreshToken;

}
