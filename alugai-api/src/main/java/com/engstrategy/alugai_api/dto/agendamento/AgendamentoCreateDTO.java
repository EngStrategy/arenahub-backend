package com.engstrategy.alugai_api.dto.agendamento;

import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class AgendamentoCreateDTO {

    @NotNull
    private Long quadraId;
    @NotNull
    private LocalDate dataAgendamento;
    @NotNull
    private Long intervaloHorarioId;
    @NotNull
    private TipoEsporte esporte;
    private boolean isFixo = false;
    private PeriodoAgendamento periodoAgendamentoFixo;
    private boolean isPublico = false;
    private Integer numeroJogadoresNecessarios;
    /*
        Lógica de isFixo e isPublico foram as seguintes, como vai ter um checkbox para essas duas opções, se ele
        não marcar, o padrão é que ambos são falsos, caso ele marque, o front vai enviar um JSON com true nesse campo
        e aí a classe de serviço vai lidar com isso (logica)
     */
}
