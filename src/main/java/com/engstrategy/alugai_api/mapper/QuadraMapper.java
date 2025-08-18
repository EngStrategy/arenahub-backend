package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.quadra.*;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;

import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.repository.HorarioFuncionamentoRepository;
import com.engstrategy.alugai_api.service.impl.SlotHorarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuadraMapper {

    public Quadra mapQuadraCreateDtoToQuadra(QuadraCreateDTO quadraCreateDTO) {
        // Map provided HorarioFuncionamento
        Map<DiaDaSemana, HorarioFuncionamento> horarioMap = quadraCreateDTO.getHorariosFuncionamento()
                .stream()
                .collect(Collectors.toMap(
                        HorarioFuncionamentoCreateDTO::getDiaDaSemana,
                        this::mapHorarioFuncionamentoCreateDtoToEntity
                ));

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

        Set<IntervaloHorario> intervalos = dto.getIntervalosDeHorario()
                .stream()
                .map(intervaloDto -> IntervaloHorario.builder()
                        .inicio(intervaloDto.getInicio())
                        .fim(intervaloDto.getFim())
                        .valor(intervaloDto.getValor())
                        .horarioFuncionamento(horario)
                        .status(intervaloDto.getStatus())
                        .build())
                .collect(Collectors.toSet());

        horario.setIntervalosDeHorario(intervalos);
        return horario;
    }

    public QuadraResponseDTO mapQuadraToQuadraResponseDTO(Quadra quadra) {
        return QuadraResponseDTO.builder()
                .id(quadra.getId())
                .nomeQuadra(quadra.getNomeQuadra())
                .urlFotoQuadra(quadra.getUrlFotoQuadra())
                .tipoQuadra(quadra.getTipoQuadra())
                .descricao(quadra.getDescricao())
                .duracaoReserva(quadra.getDuracaoReserva())
                .cobertura(quadra.isCobertura())
                .iluminacaoNoturna(quadra.isIluminacaoNoturna())
                .materiaisFornecidos(quadra.getMateriaisFornecidos())
                .arenaId(quadra.getArena().getId())
                .nomeArena(quadra.getArena().getNome())
                .build();
    }

    public QuadraResponseDTO mapQuadraToQuadraResponseDTOComHorarioFuncionamento(Quadra quadra) {
        List<HorarioFuncionamentoResponseDTO> horariosFuncionamento = quadra.getHorariosFuncionamento()
                .stream()
                .map(this::mapHorarioFuncionamentoToResponseDto)
                .collect(Collectors.toList());

        return QuadraResponseDTO.builder()
                .id(quadra.getId())
                .nomeQuadra(quadra.getNomeQuadra())
                .urlFotoQuadra(quadra.getUrlFotoQuadra())
                .tipoQuadra(quadra.getTipoQuadra())
                .descricao(quadra.getDescricao())
                .duracaoReserva(quadra.getDuracaoReserva())
                .cobertura(quadra.isCobertura())
                .iluminacaoNoturna(quadra.isIluminacaoNoturna())
                .materiaisFornecidos(quadra.getMateriaisFornecidos())
                .arenaId(quadra.getArena().getId())
                .nomeArena(quadra.getArena().getNome())
                .horariosFuncionamento(horariosFuncionamento)
                .build();
    }

    private HorarioFuncionamentoResponseDTO mapHorarioFuncionamentoToResponseDto(HorarioFuncionamento horario) {
        List<IntervaloHorarioResponseDTO> intervalos = horario.getIntervalosDeHorario()
                .stream()
                .map(intervalo -> IntervaloHorarioResponseDTO.builder()
                        .id(intervalo.getId())
                        .inicio(intervalo.getInicio())
                        .fim(intervalo.getFim())
                        .valor(intervalo.getValor())
                        .status(intervalo.getStatus())
                        .build())
                .collect(Collectors.toList());

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
