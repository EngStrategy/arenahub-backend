package com.engstrategy.alugai_api.dto.agendamento.arena;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipanteDTO {
    private UUID id;
    private String nome;
    private String email;
    private String telefone;
    private LocalDateTime dataEntrada;
}
