package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface SlotHorarioRepository extends JpaRepository<SlotHorario, Long> {
    List<SlotHorario> findByIntervaloHorario_HorarioFuncionamento_DiaDaSemanaAndHorarioInicioAndHorarioFim(
            DiaDaSemana diaSemana, LocalTime inicio, LocalTime fim
    );
}