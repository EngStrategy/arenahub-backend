package com.engstrategy.arenahub_api.repository;

import com.engstrategy.arenahub_api.model.SlotHorario;
import com.engstrategy.arenahub_api.model.enums.DiaDaSemana;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface SlotHorarioRepository extends JpaRepository<SlotHorario, Long> {
    List<SlotHorario> findByIntervaloHorario_HorarioFuncionamento_DiaDaSemanaAndHorarioInicioAndHorarioFim(
            DiaDaSemana diaSemana, LocalTime inicio, LocalTime fim
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SlotHorario s WHERE s.id IN :ids")
    List<SlotHorario> findAllByIdWithLock(@Param("ids") List<Long> ids);
}
