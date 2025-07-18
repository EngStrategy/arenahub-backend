package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.service.CidadesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CidadesServiceImpl implements CidadesService {

    private final ArenaRepository arenaRepository;

    @Override
    public Page<String> listarCidades(Pageable pageable) {
        return arenaRepository.findDistinctCidades(pageable);
    }
}
