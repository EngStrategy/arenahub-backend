package com.engstrategy.alugai_api.dto.aula;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record DetalheDiaAulaDTO(
        @NotNull(message = "O dia da semana é obrigatório.")
        DiaDaSemana diaDaSemana,

        @NotNull(message = "O horário de início é obrigatório.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horarioInicio,

        @NotNull(message = "A duração da reserva é obrigatória.")
        DuracaoReserva duracaoReserva
) {}