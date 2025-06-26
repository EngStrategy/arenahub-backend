package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.quadra.*;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.repository.HorarioFuncionamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuadraMapper {

    private final HorarioFuncionamentoRepository horarioFuncionamentoRepository;

    public Quadra mapQuadraCreateDtoToQuadra(QuadraCreateDTO quadraCreateDTO) {
        // Map provided HorarioFuncionamento
        Map<DiaDaSemana, HorarioFuncionamento> horarioMap = quadraCreateDTO.getHorariosFuncionamento()
                .stream()
                .collect(Collectors.toMap(
                        HorarioFuncionamentoCreateDTO::getDiaDaSemana,
                        this::mapHorarioFuncionamentoCreateDtoToEntity
                ));

        // Create HorarioFuncionamento for all days
        List<HorarioFuncionamento> horariosFuncionamento = new ArrayList<>();
        for (DiaDaSemana dia : DiaDaSemana.values()) {
            HorarioFuncionamento horario = horarioMap.getOrDefault(dia, HorarioFuncionamento.builder()
                    .diaDaSemana(dia)
                    .intervalosDeHorario(new ArrayList<>())
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

        List<IntervaloHorario> intervalos = dto.getIntervalosDeHorario()
                .stream()
                .map(intervaloDto -> IntervaloHorario.builder()
                        .inicio(intervaloDto.getInicio())
                        .fim(intervaloDto.getFim())
                        .valor(intervaloDto.getValor())
                        .horarioFuncionamento(horario)
                        .status(intervaloDto.getStatus())
                        .build())
                .collect(Collectors.toList());

        horario.setIntervalosDeHorario(intervalos);
        return horario;
    }

    public QuadraResponseDTO mapQuadraToQuadraResponseDTO(Quadra quadra) {
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

    public void updateQuadraFromDto(QuadraUpdateDTO updateDTO, Quadra quadra) {
        // Update simple attributes if provided
        if (updateDTO.getNomeQuadra() != null) {
            quadra.setNomeQuadra(updateDTO.getNomeQuadra());
        }
        if (updateDTO.getUrlFotoQuadra() != null) {
            quadra.setUrlFotoQuadra(updateDTO.getUrlFotoQuadra());
        }
        if (updateDTO.getTipoQuadra() != null) {
            quadra.setTipoQuadra(updateDTO.getTipoQuadra());
        }
        if (updateDTO.getDescricao() != null) {
            quadra.setDescricao(updateDTO.getDescricao());
        }
        if (updateDTO.getDuracaoReserva() != null) {
            quadra.setDuracaoReserva(updateDTO.getDuracaoReserva());
        }
        if (updateDTO.getCobertura() != null) {
            quadra.setCobertura(updateDTO.getCobertura());
        }
        if (updateDTO.getIluminacaoNoturna() != null) {
            quadra.setIluminacaoNoturna(updateDTO.getIluminacaoNoturna());
        }
        if (updateDTO.getMateriaisFornecidos() != null) {
            quadra.setMateriaisFornecidos(updateDTO.getMateriaisFornecidos());
        }

        // Update HorarioFuncionamento if provided
        if (updateDTO.getHorariosFuncionamento() != null && !updateDTO.getHorariosFuncionamento().isEmpty()) {
            // Map existing HorarioFuncionamento by day
            Map<DiaDaSemana, HorarioFuncionamento> existingHorarios = quadra.getHorariosFuncionamento()
                    .stream()
                    .collect(Collectors.toMap(HorarioFuncionamento::getDiaDaSemana, Function.identity()));

            // Process updated HorarioFuncionamento
            for (HorarioFuncionamentoUpdateDTO updateHorarioDTO : updateDTO.getHorariosFuncionamento()) {
                DiaDaSemana dia = updateHorarioDTO.getDiaDaSemana();
                HorarioFuncionamento horario = existingHorarios.get(dia);
                if (horario == null) {
                    throw new IllegalArgumentException("Horário de funcionamento não encontrado para o dia: " + dia);
                }

                // Map existing IntervaloHorario by ID
                Map<Long, IntervaloHorario> existingIntervals = horario.getIntervalosDeHorario()
                        .stream()
                        .collect(Collectors.toMap(IntervaloHorario::getId, Function.identity()));

                // Clear existing intervals to ensure removals are detected
                horario.getIntervalosDeHorario().clear();

                // Process provided intervals
                if (updateHorarioDTO.getIntervalosDeHorario() != null && !updateHorarioDTO.getIntervalosDeHorario().isEmpty()) {
                    List<IntervaloHorario> updatedIntervals = new ArrayList<>();
                    for (IntervaloHorarioUpdateDTO intervalDTO : updateHorarioDTO.getIntervalosDeHorario()) {
                        IntervaloHorario intervalo;
                        if (intervalDTO.getId() != null && existingIntervals.containsKey(intervalDTO.getId())) {
                            // Update existing interval
                            intervalo = existingIntervals.get(intervalDTO.getId());
                            if (intervalDTO.getInicio() != null) {
                                intervalo.setInicio(intervalDTO.getInicio());
                            }
                            if (intervalDTO.getFim() != null) {
                                intervalo.setFim(intervalDTO.getFim());
                            }
                            if (intervalDTO.getValor() != null) {
                                intervalo.setValor(intervalDTO.getValor());
                            }
                            if (intervalDTO.getStatus() != null) {
                                intervalo.setStatus(intervalDTO.getStatus());
                            }
                        } else {
                            // Create new interval
                            intervalo = IntervaloHorario.builder()
                                    .inicio(intervalDTO.getInicio())
                                    .fim(intervalDTO.getFim())
                                    .valor(intervalDTO.getValor())
                                    .status(intervalDTO.getStatus())
                                    .horarioFuncionamento(horario)
                                    .build();
                        }
                        updatedIntervals.add(intervalo);
                    }
                    // Add updated intervals
                    horario.getIntervalosDeHorario().addAll(updatedIntervals);
                }

                // Save HorarioFuncionamento to ensure changes are persisted
                horarioFuncionamentoRepository.save(horario);
            }
        }
    }
}
