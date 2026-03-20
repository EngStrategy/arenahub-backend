package com.engstrategy.arenahub_api.repository;

import com.engstrategy.arenahub_api.model.SlotHorario;
import com.engstrategy.arenahub_api.model.enums.DiaDaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface SlotHorarioRepository extends JpaRepository<SlotHorario, Long> {
    List<SlotHorario> findByIntervaloHorario_HorarioFuncionamento_DiaDaSemanaAndHorarioInicioAndHorarioFim(
            DiaDaSemana diaSemana, LocalTime inicio, LocalTime fim
    );
}