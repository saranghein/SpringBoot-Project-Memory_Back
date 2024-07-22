package com.memory.meco;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MecoRepository extends JpaRepository<Meco, Long> {
    Optional<Meco> findByMecoDateAndUserId(LocalDateTime date, String userId);
}

