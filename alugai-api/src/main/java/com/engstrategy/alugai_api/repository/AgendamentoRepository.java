package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Quadra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByQuadraIdAndDataAgendamento(Long quadraId, LocalDate dataAgendamento);

    // Verifica se um agendamento exato já existe.
    boolean existsByQuadraAndDataAgendamentoAndInicioAndFim(
            Quadra quadra, LocalDate dataAgendamento, LocalTime inicio, LocalTime fim);

    // Retorna uma página de agendamentos associados a um ID de atleta.
    Page<Agendamento> findByAtletaId(Long atletaId, Pageable pageable);
}
