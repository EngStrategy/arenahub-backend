package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.arena.ArenaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaResponseDTO;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/v1/usuarios")
@Tag(name = "Usuário", description = "Endpoints para gerenciamento de dados do usuário autenticado")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário logado",
            description = "Retorna os dados do usuário atualmente autenticado (atleta ou arena)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ArenaResponseDTO.class),
                                    examples = @ExampleObject(name = "Arena",
                                            summary = "Exemplo de resposta para Arena",
                                            value = """
                                    {
                                        "id": 1,
                                        "nome": "Arena Sports",
                                        "email": "contato@arenasports.com",
                                        "telefone": "(11) 99999-9999",
                                        "role": "ARENA"
                                    }
                                    """)),
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AtletaResponseDTO.class),
                                    examples = @ExampleObject(name = "Atleta",
                                            summary = "Exemplo de resposta para Atleta",
                                            value = """
                                    {
                                        "id": 1,
                                        "nome": "João Silva",
                                        "email": "joao@email.com",
                                        "telefone": "(11) 88888-8888",
                                        "role": "ATLETA"
                                    }
                                    """))
                    }),
            @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou expirado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Object> getCurrentUser(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        if (usuario instanceof Arena) {
            // Retornar dados específicos da arena
            Arena arena = (Arena) usuario;
            return ResponseEntity.ok(mapArenaToResponse(arena));
        } else if (usuario instanceof Atleta) {
            // Retornar dados específicos do atleta
            Atleta atleta = (Atleta) usuario;
            return ResponseEntity.ok(mapAtletaToResponse(atleta));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private Object mapArenaToResponse(Arena arena) {
        // Usar seu ArenaMapper aqui
        return ArenaResponseDTO.builder()
                .id(arena.getId())
                .nome(arena.getNome())
                .email(arena.getEmail())
                .telefone(arena.getTelefone())
                .role(arena.getRole())
                .build();
    }

    private Object mapAtletaToResponse(Atleta atleta) {
        // Usar seu AtletaMapper aqui
        return AtletaResponseDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNome())
                .email(atleta.getEmail())
                .telefone(atleta.getTelefone())
                .role(atleta.getRole())
                .build();
    }
}
