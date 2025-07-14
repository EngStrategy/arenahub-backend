package com.engstrategy.alugai_api.dto.agendamento;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Não inclui campos nulos no JSON
public class MeuAgendamentoResponseDTO {

    // Dados comuns
    private Long id; // Pode ser o ID do Agendamento ou da Solicitação
    private String nomeArena;
    private String nomeQuadra;
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private BigDecimal valor; // Pode ser o valor total ou o valor por pessoa

    // Status
    private String status; // Um texto para exibir na tela: "Pendente", "Confirmado", "Cancelado", "Aceito", "Recusado"

    // Distinção
    private boolean criadoPorMim; // true se o usuário for o dono do agendamento
    private Integer vagasDisponiveis; // Apenas para jogos criados por mim
    private Long agendamentoId; // ID do agendamento original, útil para navegação
}