package com.engstrategy.alugai_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CidadesService {
    Page<String> listarCidades(Pageable pageable);
}
