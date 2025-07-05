package com.engstrategy.alugai_api.repository;

import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SlotHorarioRepository extends JpaRepository<SlotHorario, Long> {

    List<SlotHorario> findByIntervaloHorario(IntervaloHorario intervaloHorario);

    void deleteByIntervaloHorario(IntervaloHorario intervaloHorario);

    @Query("SELECT s FROM SlotHorario s WHERE s.intervaloHorario.horarioFuncionamento.quadra.id = :quadraId " +
            "AND s.intervaloHorario.horarioFuncionamento.diaDaSemana = :diaSemana")
    List<SlotHorario> findByQuadraAndDiaSemana(@Param("quadraId") Long quadraId,
                                               @Param("diaSemana") DiaDaSemana diaSemana);

    @Query("SELECT s FROM SlotHorario s WHERE s.statusDisponibilidade = 'DISPONIVEL' " +
            "AND s.intervaloHorario.horarioFuncionamento.quadra = :quadra")
    List<SlotHorario> findSlotsDisponiveisByQuadra(@Param("quadra") Quadra quadra);

    Optional<SlotHorario> findByIntervaloHorario_HorarioFuncionamento_DiaDaSemanaAndHorarioInicioAndHorarioFim(
            DiaDaSemana diaSemana, LocalTime inicio, LocalTime fim
    );
}