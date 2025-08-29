package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.arena.EnderecoDTO;
import com.engstrategy.alugai_api.model.Endereco;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

    public Endereco mapEnderecoDtoToEndereco(EnderecoDTO enderecoDTO) {
        return Endereco.builder()
                .cep(enderecoDTO.getCep())
                .estado(enderecoDTO.getEstado())
                .cidade(enderecoDTO.getCidade())
                .bairro(enderecoDTO.getBairro())
                .rua(enderecoDTO.getRua())
                .numero(enderecoDTO.getNumero())
                .complemento(enderecoDTO.getComplemento())
                .latitude(enderecoDTO.getLatitude())
                .longitude(enderecoDTO.getLongitude())
                .build();
    }

    public EnderecoDTO mapEnderecoToEnderecoDTO(Endereco endereco) {
        return EnderecoDTO.builder()
                .cep(endereco.getCep())
                .estado(endereco.getEstado())
                .cidade(endereco.getCidade())
                .bairro(endereco.getBairro())
                .rua(endereco.getRua())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .latitude(endereco.getLatitude())
                .longitude(endereco.getLongitude())
                .build();
    }
}
