package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioFuncionamento {

    private String diaDaSemana;
    private String horarios;
    private double valor;
    private StatusAgendamento status;
}