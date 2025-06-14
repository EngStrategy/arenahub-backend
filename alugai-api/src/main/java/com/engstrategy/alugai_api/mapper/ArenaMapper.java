package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.arena.ArenaCreateDTO;
import com.engstrategy.alugai_api.dto.arena.ArenaResponseDTO;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArenaMapper {

    private final EnderecoMapper enderecoMapper;

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
                .build();
    }

    public ArenaResponseDTO mapArenaToArenaResponseDTO(Arena arena) {
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
                .build();
    }
}
