package com.memory.meco;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<MecoResponse> getMecoByDateAndUserId(LocalDateTime date, String userId) {
        return mecoRepository.findByMecoDateAndUserId(date, userId)
                .map(MecoResponse::fromMeco);
    }
}
