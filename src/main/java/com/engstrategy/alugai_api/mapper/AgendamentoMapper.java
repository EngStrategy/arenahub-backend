package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoExternoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoFixoResponseDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoResponseDTO;
import com.engstrategy.alugai_api.dto.agendamento.arena.AgendamentoArenaResponseDTO;
import com.engstrategy.alugai_api.dto.agendamento.arena.ParticipanteDTO;
import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoDetalhesDTO;
import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.QuadraRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AgendamentoMapper {

    private final QuadraRepository quadraRepository;

    public Agendamento fromCreateToAgendamento(AgendamentoCreateDTO dto, Set<SlotHorario> slots, Atleta atleta) {
        Quadra quadra = quadraRepository.findById(dto.getQuadraId())
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + dto.getQuadraId()));

        return Agendamento.builder()
                .dataAgendamento(dto.getDataAgendamento())
                .esporte(dto.getEsporte())
                .isFixo(dto.isFixo())
                .isPublico(dto.isPublico())
                .periodoAgendamentoFixo(dto.getPeriodoFixo())
                .vagasDisponiveis(dto.getNumeroJogadoresNecessarios())
                .slotsHorario(slots)
                .status(StatusAgendamento.PENDENTE)
                .atleta(atleta)
                .quadra(quadra)
                .build();
    }

    public AgendamentoResponseDTO fromAgendamentoToResponseDTO(Agendamento agendamento) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (agendamento == null) {
            return null;
        }

        Avaliacao avaliacaoEntidade = agendamento.getAvaliacao();
        AvaliacaoDetalhesDTO avaliacaoDTO = null;
        if (avaliacaoEntidade != null) {
            avaliacaoDTO = AvaliacaoDetalhesDTO.builder()
                    .idAvaliacao(avaliacaoEntidade.getId())
                    .nota(avaliacaoEntidade.getNota())
                    .comentario(avaliacaoEntidade.getComentario())
                    .build();
        }

        return AgendamentoResponseDTO.builder()
                .id(agendamento.getId())
                .dataAgendamento(agendamento.getDataAgendamento().format(formatter))
                .horarioInicio(agendamento.getHorarioInicio())
                .horarioFim(agendamento.getHorarioFim())
                .valorTotal(agendamento.getValorTotal())
                .esporte(agendamento.getEsporte())
                .status(agendamento.getStatus())
                .numeroJogadoresNecessarios(agendamento.getVagasDisponiveis())
                .slotsHorario(agendamento.getSlotsHorario().stream()
                        .map(this::mapearSlotParaResponse)
                        .collect(Collectors.toSet()))
                .quadraId(agendamento.getQuadra().getId())
                .nomeQuadra(agendamento.getQuadra().getNomeQuadra())
                .nomeArena(agendamento.getQuadra().getArena().getNome())
                .urlFotoQuadra(agendamento.getQuadra().getUrlFotoQuadra())
                .urlFotoArena(agendamento.getQuadra().getArena().getUrlFoto())
                .nomeQuadra(agendamento.getQuadra().getNomeQuadra())
                .fixo(agendamento.isFixo())
                .agendamentoFixoId(agendamento.getAgendamentoFixo() != null ? agendamento.getAgendamentoFixo().getId() : null)
                .publico(agendamento.isPublico())
                .possuiSolicitacoes(agendamento.possuiSolicitacoes())
                .avaliacao(avaliacaoDTO)
                .avaliacaoDispensada(agendamento.getAvaliacaoDispensada())
                .build();
    }

    private SlotHorarioResponseDTO mapearSlotParaResponse(SlotHorario slot) {
        return SlotHorarioResponseDTO.builder()
                .id(slot.getId())
                .horarioInicio(slot.getHorarioInicio())
                .horarioFim(slot.getHorarioFim())
                .valor(slot.getValor())
                .statusDisponibilidade(slot.getStatusDisponibilidade())
                .build();
    }

    public AgendamentoArenaResponseDTO fromAgendamentoToArenaResponseDTO(Agendamento agendamento) {
        if (agendamento == null) {
            return null;
        }

        return AgendamentoArenaResponseDTO.builder()
                .id(agendamento.getId())
                .dataAgendamento(agendamento.getDataAgendamento())
                .horarioInicio(agendamento.getHorarioInicio())
                .horarioFim(agendamento.getHorarioFim())
                .valorTotal(agendamento.getValorTotal())
                .status(agendamento.getStatus())
                .isFixo(agendamento.isFixo())
                .isPublico(agendamento.isPublico())
                .vagasDisponiveis(agendamento.getVagasDisponiveis())
                .esporte(agendamento.getEsporte())
                .quadraId(agendamento.getQuadra().getId())
                .nomeQuadra(agendamento.getQuadra().getNomeQuadra())
                .atletaId(agendamento.getAtleta().getId())
                .nomeAtleta(agendamento.getAtleta().getNome())
                .emailAtleta(agendamento.getAtleta().getEmail())
                .telefoneAtleta(agendamento.getAtleta().getTelefone())
                .urlFotoAtleta(agendamento.getAtleta().getUrlFoto())
                .totalParticipantes(agendamento.getParticipantes() != null ?
                        agendamento.getParticipantes().size() : 0)
                .participantes(mapParticipantes(agendamento.getParticipantes()))
                .slotsHorario(mapSlotsHorario(agendamento.getSlotsHorario()))
                .build();
    }

    private List<ParticipanteDTO> mapParticipantes(Set<Atleta> participantes) {
        if (participantes == null || participantes.isEmpty()) {
            return new ArrayList<>();
        }

        return participantes.stream()
                .map(this::mapParticipante)
                .collect(Collectors.toList());
    }

    private ParticipanteDTO mapParticipante(Atleta atleta) {
        return ParticipanteDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNome())
                .email(atleta.getEmail())
                .telefone(atleta.getTelefone())
                .dataEntrada(atleta.getDataCriacao()) // Assumindo que existe este campo
                .build();
    }

    private Set<SlotHorarioResponseDTO> mapSlotsHorario(Set<SlotHorario> slotsHorario) {
        if (slotsHorario == null || slotsHorario.isEmpty()) {
            return new HashSet<>();
        }

        return slotsHorario.stream()
                .map(this::mapSlotHorario)
                .sorted(Comparator.comparing(SlotHorarioResponseDTO::getHorarioInicio))
                .collect(Collectors.toSet());
    }

    private SlotHorarioResponseDTO mapSlotHorario(SlotHorario slotHorario) {
        return SlotHorarioResponseDTO.builder()
                .id(slotHorario.getId())
                .horarioInicio(slotHorario.getHorarioInicio())
                .horarioFim(slotHorario.getHorarioFim())
                .valor(slotHorario.getValor())
                .statusDisponibilidade(slotHorario.getStatusDisponibilidade())
                .build();
    }

    /**
     * Converte um DTO de agendamento externo para o DTO de criação de agendamento padrão.
     * Define valores padrão para campos que não se aplicam a agendamentos externos.
     * @param externoDTO O DTO recebido do endpoint de agendamento externo.
     * @return Um AgendamentoCreateDTO pronto para ser usado pelo método de criação principal.
     */
    public AgendamentoCreateDTO fromExternoToCreateDTO(AgendamentoExternoCreateDTO externoDTO) {
        AgendamentoCreateDTO createDTO = new AgendamentoCreateDTO();

        createDTO.setQuadraId(externoDTO.getQuadraId());
        createDTO.setDataAgendamento(externoDTO.getDataAgendamento());
        createDTO.setSlotHorarioIds(externoDTO.getSlotHorarioIds());
        createDTO.setEsporte(externoDTO.getEsporte());

        // Ele nunca será 'fixo' ou 'público' neste contexto
        createDTO.setFixo(false);
        createDTO.setPublico(false);
        createDTO.setNumeroJogadoresNecessarios(null);
        createDTO.setPeriodoFixo(null);

        return createDTO;
    }
}