package com.memory.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    @Modifying
    @Query("UPDATE User u SET u.answers = :answers WHERE u.userId = :userId")
    void updateAnswers(@Param("userId") String userId, @Param("answers") List<String> answers);
}
