package com.memory.meco;

import com.memory.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.Optional;

@Service
public class MecoService {
    MecoRepository mecoRepository;
    @Autowired
    public MecoService(MecoRepository mecoRepository) {
        this.mecoRepository = mecoRepository;
    }
    public void saveMeco(Meco meco) {
        mecoRepository.save(meco);
    }

    public Optional<MecoResponse> getMecoByDateAndUserId(LocalDate date, User user) {
        return mecoRepository.findByMecoDateAndUser(date, user)
                .map(MecoResponse::fromMeco);
    }
}
