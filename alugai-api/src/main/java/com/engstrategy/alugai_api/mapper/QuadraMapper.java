package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.quadra.*;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuadraMapper {

    public Quadra mapQuadraCreateDtoToQuadra(QuadraCreateDTO quadraCreateDTO) {
        List<HorarioFuncionamento> horariosFuncionamento = quadraCreateDTO.getHorariosFuncionamento()
                .stream()
                .map(this::mapHorarioFuncionamentoCreateDtoToEntity)
                .collect(Collectors.toList());

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
                        .inicio(intervalo.getInicio())
                        .fim(intervalo.getFim())
                        .valor(intervalo.getValor())
                        .build())
                .collect(Collectors.toList());

        return HorarioFuncionamentoResponseDTO.builder()
                .diaDaSemana(horario.getDiaDaSemana())
                .intervalosDeHorario(intervalos)
                .build();
    }
}
