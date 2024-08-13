package com.memory.meco.service;

import com.memory.meco.Meco;
import com.memory.meco.MecoRepository;
import com.memory.meco.dto.MecoResponse;
import com.memory.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.Optional;

@Service
public class MecoServiceImpl implements MecoService {
    MecoRepository mecoRepository;
    @Autowired
    public MecoServiceImpl(MecoRepository mecoRepository) {
        this.mecoRepository = mecoRepository;
    }

    @Override
    public void saveMeco(Meco meco) {
        mecoRepository.save(meco);
    }

    @Override
    public Optional<MecoResponse> getMecoByDateAndUserId(LocalDate date, User user) {
        return mecoRepository.findByMecoDateAndUser(date, user)
                .map(MecoResponse::fromMeco);
    }
}
