package com.engstrategy.alugai_api.dto.arena;

import com.engstrategy.alugai_api.dto.quadra.QuadraResponseDTO;
import com.engstrategy.alugai_api.model.enums.Role;
import com.engstrategy.alugai_api.model.enums.StatusAssinatura;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Resposta com dados da arena")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArenaResponseDTO {

    @Schema(description = "ID da arena", example = "1")
    private UUID id;

    @Schema(description = "Nome da arena", example = "Arena Sports Center")
    private String nome;

    @Schema(description = "Email da arena", example = "contato@arena.com")
    private String email;

    @Schema(description = "Telefone da arena", example = "(11) 99999-9999")
    private String telefone;

    @Schema(description = "Endereço da arena")
    private EnderecoDTO endereco;

    @Schema(description = "Descrição da arena")
    private String descricao;

    @Schema(description = "URL da foto da arena")
    private String urlFoto;

    @Schema(description = "Data de criação", example = "2024-01-01T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Role do usuário", example = "ARENA")
    private Role role;

    @Schema(description = "Esportes", example = "FUTEBOL_SOCIETY")
    private List<TipoEsporte> esportes;

    @Schema(description = "Quadras da arena")
    private List<QuadraResponseDTO> quadras;

    @Schema(description = "Media da nota de avaliação da arena", example = "4.5")
    private Double notaMedia;

    @Schema(description = "Hora de antecedência de agendamento", example = "2")
    private Integer horasCancelarAgendamento;

    @Schema(description = "Quantidade de avaliações", example = "10")
    private Long quantidadeAvaliacoes;

    @Schema(description = "Status da assinatura", example = "ATIVO")
    private StatusAssinatura statusAssinatura;
}
