package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.quadra.*;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuadraMapper {

    public Quadra mapQuadraCreateDtoToQuadra(QuadraCreateDTO quadraCreateDTO) {
        // Map provided HorarioFuncionamento
        Map<DiaDaSemana, HorarioFuncionamento> horarioMap = new HashMap<>();

        if (quadraCreateDTO.getHorariosFuncionamento() != null) {
            horarioMap = quadraCreateDTO.getHorariosFuncionamento()
                .stream()
                .collect(Collectors.toMap(
                    HorarioFuncionamentoCreateDTO::getDiaDaSemana,
                    this::mapHorarioFuncionamentoCreateDtoToEntity
                ));
        }

        // Create HorarioFuncionamento for all days
        Set<HorarioFuncionamento> horariosFuncionamento = new HashSet<>();
        for (DiaDaSemana dia : DiaDaSemana.values()) {
            HorarioFuncionamento horario = horarioMap.getOrDefault(dia, HorarioFuncionamento.builder()
                .diaDaSemana(dia)
                .intervalosDeHorario(new HashSet<>())
                .build());
            horariosFuncionamento.add(horario);
        }

        return Quadra.builder()
            .nomeQuadra(quadraCreateDTO.getNomeQuadra())
            .urlFotoQuadra(quadraCreateDTO.getUrlFotoQuadra())
            .tipoQuadra(quadraCreateDTO.getTipoQuadra())
            .descricao(quadraCreateDTO.getDescricao())
            .duracaoReserva(quadraCreateDTO.getDuracaoReserva())
            .cobertura(quadraCreateDTO.isCobertura())
            .iluminacaoNoturna(quadraCreateDTO.isIluminacaoNoturna())
            .materiaisFornecidos(quadraCreateDTO.getMateriaisFornecidos())
            .horariosFuncionamento(horariosFuncionamento)
            .build();
    }

    private HorarioFuncionamento mapHorarioFuncionamentoCreateDtoToEntity(HorarioFuncionamentoCreateDTO dto) {
        HorarioFuncionamento horario = HorarioFuncionamento.builder()
            .diaDaSemana(dto.getDiaDaSemana())
            .build();

        Set<IntervaloHorario> intervalos = new HashSet<>();

        if (dto.getIntervalosDeHorario() != null) {
            intervalos = dto.getIntervalosDeHorario()
                .stream()
                .map(intervaloDto -> IntervaloHorario.builder()
                    .inicio(intervaloDto.getInicio())
                    .fim(intervaloDto.getFim())
                    .valor(intervaloDto.getValor())
                    .horarioFuncionamento(horario)
                    .status(intervaloDto.getStatus())
                    .build())
                .collect(Collectors.toSet());
        }

        horario.setIntervalosDeHorario(intervalos);
        return horario;
    }

    // --- MÉTODOS DE RESPOSTA (ONDE OCORRIA O ERRO) ---

    public QuadraResponseDTO mapQuadraToQuadraResponseDTO(Quadra quadra) {
        // Usa a lógica blindada
        return buildQuadraResponseDTO(quadra);
    }

    public QuadraResponseDTO mapQuadraToQuadraResponseDTOComHorarioFuncionamento(Quadra quadra) {
        // CORRIGIDO: Este método estava vulnerável.
        // Agora ele reutiliza a mesma lógica segura do método acima.
        return buildQuadraResponseDTO(quadra);
    }

    // Método auxiliar privado para centralizar a lógica segura e evitar duplicidade
    private QuadraResponseDTO buildQuadraResponseDTO(Quadra quadra) {

        // 1. Tratamento defensivo para a lista de horários
        Set<HorarioFuncionamentoResponseDTO> horariosMapeados = new LinkedHashSet<>();

        if (quadra.getHorariosFuncionamento() != null) {
            horariosMapeados = quadra.getHorariosFuncionamento()
                .stream()
                .filter(Objects::nonNull)
                .map(this::mapHorarioFuncionamentoToResponseDto)
                .sorted(Comparator.comparing(dto -> dto.getDiaDaSemana().ordinal()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        // 2. Tratamento defensivo para a Arena (Pai)
        String nomeArena = "N/A";
        UUID arenaId = null;

        if (quadra.getArena() != null) {
            arenaId = quadra.getArena().getId();
            try {
                nomeArena = quadra.getArena().getNome();
            } catch (Exception e) {
                // Ignora erro de Lazy Loading se ocorrer
            }
        }

        return QuadraResponseDTO.builder()
            .id(quadra.getId())
            .nomeQuadra(quadra.getNomeQuadra())
            .urlFotoQuadra(quadra.getUrlFotoQuadra())
            .tipoQuadra(quadra.getTipoQuadra() != null
                ? new HashSet<>(quadra.getTipoQuadra())
                : new HashSet<>())
            .descricao(quadra.getDescricao())
            .duracaoReserva(quadra.getDuracaoReserva())
            .cobertura(quadra.isCobertura())
            .iluminacaoNoturna(quadra.isIluminacaoNoturna())
            .materiaisFornecidos(quadra.getMateriaisFornecidos() != null
                ? new HashSet<>(quadra.getMateriaisFornecidos())
                : new HashSet<>())
            .arenaId(arenaId)
            .nomeArena(nomeArena)
            .horariosFuncionamento(horariosMapeados)
            .build();
    }

    private HorarioFuncionamentoResponseDTO mapHorarioFuncionamentoToResponseDto(HorarioFuncionamento horario) {
        // 3. BLINDAGEM: Verifica se intervalosDeHorario é null
        List<IntervaloHorarioResponseDTO> intervalos = new ArrayList<>();

        if (horario.getIntervalosDeHorario() != null) {
            intervalos = horario.getIntervalosDeHorario()
                .stream()
                .filter(Objects::nonNull)
                .map(intervalo -> IntervaloHorarioResponseDTO.builder()
                    .id(intervalo.getId())
                    .inicio(intervalo.getInicio())
                    .fim(intervalo.getFim())
                    .valor(intervalo.getValor())
                    .status(intervalo.getStatus())
                    .build())
                .collect(Collectors.toList());
        }

        return HorarioFuncionamentoResponseDTO.builder()
            .id(horario.getId())
            .diaDaSemana(horario.getDiaDaSemana())
            .intervalosDeHorario(intervalos)
            .build();
    }

    public SlotHorarioResponseDTO mapearSlotParaResponse(SlotHorario slot) {
        return SlotHorarioResponseDTO.builder()
            .id(slot.getId())
            .horarioInicio(slot.getHorarioInicio())
            .horarioFim(slot.getHorarioFim())
            .valor(slot.getValor())
            .statusDisponibilidade(slot.getStatusDisponibilidade())
            .build();
    }
}