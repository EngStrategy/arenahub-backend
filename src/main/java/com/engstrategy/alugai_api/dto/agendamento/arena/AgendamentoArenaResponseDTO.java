package com.engstrategy.alugai_api.dto.agendamento.arena;

import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.engstrategy.alugai_api.util.LocalTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoArenaResponseDTO {
    private Long id;
    private LocalDate dataAgendamento;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioInicio;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime horarioFim;
    private BigDecimal valorTotal;
    private StatusAgendamento status;
    private boolean isFixo;
    private boolean isPublico;
    private Integer vagasDisponiveis;
    private TipoEsporte esporte;

    // Informações da quadra
    private Long quadraId;
    private String nomeQuadra;

    // Informações do atleta
    private UUID atletaId;
    private String nomeAtleta;
    private String emailAtleta;
    private String telefoneAtleta;
    private String urlFotoAtleta;

    // Informações dos participantes (se for público)
    private Integer totalParticipantes;
    private List<ParticipanteDTO> participantes;

    // Informações dos slots
    private Set<SlotHorarioResponseDTO> slotsHorario;
    private Long agendamentoFixoId;
}
