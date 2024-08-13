package com.memory.meco.service;

import com.memory.meco.Meco;
import com.memory.meco.dto.MecoResponse;
import com.memory.user.User;

import java.time.LocalDate;
import java.util.Optional;

public interface MecoService {
    void saveMeco(Meco meco);

    Optional<MecoResponse> getMecoByDateAndUserId(LocalDate date, User user);
}
