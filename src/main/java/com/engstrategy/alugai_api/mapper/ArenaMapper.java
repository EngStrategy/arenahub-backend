package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.arena.ArenaCreateDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaResponseDTO;
import com.engstrategy.alugai_api.dto.quadra.HorarioFuncionamentoResponseDTO;
import com.engstrategy.alugai_api.dto.quadra.IntervaloHorarioResponseDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraResponseDTO;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ArenaMapper {

    private final EnderecoMapper enderecoMapper;
    private final QuadraMapper quadraMapper;

    public Arena mapArenaCreateDtoToArena(ArenaCreateDTO arenaCreateDTO) {
        return Arena.builder()
                .nome(arenaCreateDTO.getNome())
                .email(arenaCreateDTO.getEmail())
                .telefone(arenaCreateDTO.getTelefone())
                .senha(arenaCreateDTO.getSenha())
                .cpfProprietario(arenaCreateDTO.getCpfProprietario())
                .cnpj(arenaCreateDTO.getCnpj())
                .descricao(arenaCreateDTO.getDescricao())
                .urlFoto(arenaCreateDTO.getUrlFoto())
                .endereco(enderecoMapper.mapEnderecoDtoToEndereco(arenaCreateDTO.getEndereco()))
                .role(Role.ARENA)
                .ativo(false)
                .horasCancelarAgendamento(arenaCreateDTO.getHorasCancelarAgendamento())
                .build();
    }

    public ArenaResponseDTO mapArenaToArenaResponseDTO(Arena arena) {

        List<QuadraResponseDTO> quadras = new ArrayList<>();

        if(arena.getQuadras() != null) {
             quadras = arena.getQuadras()
                    .stream()
                    .map(this::mapQuadraToQuadraResponseDTO)
                    .toList();
        }

        List<TipoEsporte> esportes = arena.getQuadras() != null
                ? arena.getQuadras()
                .stream()
                .flatMap(quadra -> quadra.getTipoQuadra().stream())
                .distinct()
                .toList()
                : new ArrayList<>();

        return ArenaResponseDTO.builder()
                .id(arena.getId())
                .nome(arena.getNome())
                .email(arena.getEmail())
                .telefone(arena.getTelefone())
                .descricao(arena.getDescricao())
                .urlFoto(arena.getUrlFoto())
                .endereco(enderecoMapper.mapEnderecoToEnderecoDTO(arena.getEndereco()))
                .dataCriacao(arena.getDataCriacao())
                .role(arena.getRole())
                .esportes(esportes)
                .horasCancelarAgendamento(arena.getHorasCancelarAgendamento())
                .stripeCustomerId(arena.getStripeCustomerId())
                .statusAssinatura(arena.getStatusAssinatura())
                .build();
    }

    private QuadraResponseDTO mapQuadraToQuadraResponseDTO(Quadra quadra) {
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
}
