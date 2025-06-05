package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioFuncionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DiaDaSemana diaDaSemana;

    @OneToMany(mappedBy = "horarioFuncionamento", cascade = CascadeType.ALL)
    private List<IntervaloHorario> intervalosDeHorario = new ArrayList<>();
}