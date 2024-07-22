package com.memory.meco;

import com.memory.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MecoRepository extends JpaRepository<Meco, Long> {
    Optional<Meco> findByMecoDateAndUser(LocalDate date, User user);
}

