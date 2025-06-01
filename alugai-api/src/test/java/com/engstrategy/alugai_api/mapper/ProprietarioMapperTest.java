package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.dto.proprietario.ProprietarioCadastroDTO;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Proprietario;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ProprietarioMapperTest {

    /**
     * Essa classe de testes visa garantir que a conversão de Entidades
     * para DTO's, e vice versa, está sendo feita corretamente utilizando mapstruct.
     * Há 4 testes unitários com cenários diferentes:
     * 1: Verifica se um ProprietarioCadastroDTO válido é mapeado corretamente para Proprietario
     * 2: Verifica se um Proprietario é mapeado corretamente para ProprietarioResponseDTO
     * 3: Verifica se toEntity(null) retorna null
     * 4: Verifica se toDto(null) retorna null
     * Não considerei cenários onde os parâmetros passados são inválidos, pois haverá
     * validação com Bean Validation antes do mapeamento.
     */

    @Autowired
    private ProprietarioMapper proprietarioMapper;

    private Proprietario proprietario;
    private ProprietarioCadastroDTO proprietarioCadastroDTO;

    @BeforeEach
    public void setUp() {

        proprietarioCadastroDTO = new ProprietarioCadastroDTO(
                "Robinho",
                "robinho@gmail.com",
                "1234567890",
                "1234",
                "1234567890"
        );

        proprietario = new Proprietario();
        proprietario.setId(UUID.randomUUID());
        proprietario.setNome("Robinho");
        proprietario.setCpf("123.456.789-10");
        proprietario.setEmail("robinho@gmail.com");
        proprietario.setSenha("1234");
        proprietario.setTelefone("1234567890");
    }

    @Test
    void shouldMapProprietarioCadastroDTOToProprietario() {

        var proprietarioEntity = proprietarioMapper.toEntity(proprietarioCadastroDTO);

        assertThat(proprietarioEntity).isNotNull();
        assertThat(proprietarioEntity.getId()).isNull();
        assertThat(proprietarioEntity.getNome()).isEqualTo(proprietarioCadastroDTO.nome());
        assertThat(proprietarioEntity.getEmail()).isEqualTo(proprietarioCadastroDTO.email());
        assertThat(proprietarioEntity.getSenha()).isEqualTo(proprietarioCadastroDTO.senha());
        assertThat(proprietarioEntity.getTelefone()).isEqualTo(proprietarioCadastroDTO.telefone());
        assertThat(proprietarioEntity.getCpf()).isEqualTo(proprietarioCadastroDTO.cpf());

    }

    @Test
    void shouldMapProprietarioToProprietarioResponseDTO(){

        var response = proprietarioMapper.toDto(proprietario);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo(proprietario.getNome());
        assertThat(response.email()).isEqualTo(proprietario.getEmail());
        assertThat(response.cpf()).isEqualTo(proprietario.getCpf());
        assertThat(response.telefone()).isEqualTo(proprietario.getTelefone());
        assertThat(response.id()).isEqualTo(proprietario.getId());

    }

    @Test
    void shouldHandleNullProprietarioCadastroDTO() {

        var proprietarioEntity = proprietarioMapper.toEntity(null);

        assertThat(proprietarioEntity).isNull();
    }

    @Test
    void shouldHandleNullProprietario() {

        var responseDto = proprietarioMapper.toDto(null);

        assertThat(responseDto).isNull();
    }


}
