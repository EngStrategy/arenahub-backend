package com.engstrategy.alugai_api.mapper;

import com.engstrategy.alugai_api.dto.atleta.AtletaCadastroDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.mapper.AtletaMapper;
import com.engstrategy.alugai_api.model.Atleta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AtletaMapperTest {

    /**
     * Essa classe de testes visa garantir que a conversão de Entidades
     * para DTO's, e vice versa, está sendo feita corretamente utilizando mapstruct.
     * Há 4 testes unitários com cenários diferentes:
     * 1: Verifica se um AtletaCadastroDTO válido é mapeado corretamente para Atleta
     * 2: Verifica se um Atleta é mapeado corretamente para AtletaResponseDTO, sem a senha
     * 3: Verifica se toEntity(null) retorna null
     * 4: Verifica se toDto(null) retorna null
     * Não considerei cenários onde os parâmetros passados são inválidos, pois haverá
     * validação com Bean Validation antes do mapeamento.
     */

    @Autowired
    private AtletaMapper atletaMapper;

    private AtletaCadastroDTO atletaCadastroDTO;
    private Atleta atleta;

    @BeforeEach
    void setUp() {

        atletaCadastroDTO = new AtletaCadastroDTO(
                "João Silva",
                "joao.silva@gmail.com",
                "11987654321",
                "senha123"
        );

        atleta = new Atleta();
        atleta.setId(UUID.randomUUID());
        atleta.setNome("João Silva");
        atleta.setEmail("joao.silva@gmail.com");
        atleta.setTelefone("11987654321");
        atleta.setSenha("senha123");
    }

    @Test
    void shouldMapAtletaCadastroDTOToAtleta() {

        Atleta atletaEntity = atletaMapper.toEntity(atletaCadastroDTO);

        assertThat(atletaEntity).isNotNull();
        assertThat(atletaEntity.getNome()).isEqualTo(atletaCadastroDTO.nome());
        assertThat(atletaEntity.getEmail()).isEqualTo(atletaCadastroDTO.email());
        assertThat(atletaEntity.getTelefone()).isEqualTo(atletaCadastroDTO.telefone());
        assertThat(atletaEntity.getSenha()).isEqualTo(atletaCadastroDTO.senha());
        assertThat(atletaEntity.getId()).isNull(); // ID não é mapeado pelo DTO
    }

    @Test
    void shouldMapAtletaToAtletaResponseDTO() {

        AtletaResponseDTO responseDTO = atletaMapper.toDto(atleta);


        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.id()).isEqualTo(atleta.getId());
        assertThat(responseDTO.nome()).isEqualTo(atleta.getNome());
        assertThat(responseDTO.email()).isEqualTo(atleta.getEmail());
        assertThat(responseDTO.telefone()).isEqualTo(atleta.getTelefone());
        assertThat(responseDTO).hasNoNullFieldsOrProperties();
    }

    @Test
    void shouldHandleNullAtletaCadastroDTO() {

        Atleta atletaEntity = atletaMapper.toEntity(null);

        assertThat(atletaEntity).isNull();
    }

    @Test
    void shouldHandleNullAtleta() {

        AtletaResponseDTO responseDTO = atletaMapper.toDto(null);

        assertThat(responseDTO).isNull();
    }
}