package com.engstrategy.arenahub_api.controller;

import com.engstrategy.arenahub_api.dto.usuario.AuthResponse;
import com.engstrategy.arenahub_api.dto.usuario.DeleteAccountRequestDTO;
import com.engstrategy.arenahub_api.dto.usuario.LoginRequest;
import com.engstrategy.arenahub_api.exceptions.InvalidCredentialsException;
import com.engstrategy.arenahub_api.exceptions.UserNotFoundException;
import com.engstrategy.arenahub_api.jwt.CustomUserDetails;
import com.engstrategy.arenahub_api.model.Usuario;
import com.engstrategy.arenahub_api.service.UserService;
import com.engstrategy.arenahub_api.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/usuarios")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/auth")
    @Operation(summary = "Autenticar usuário",
            description = "Autentica um usuário (atleta ou arena) e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<AuthResponse> auth(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        } catch (InvalidCredentialsException e) {
            throw new InvalidCredentialsException(e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados do usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AuthResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new UserNotFoundException("Nenhum usuário autenticado encontrado.");
        }

        AuthResponse userResponse = authService.findById(userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/delete-account")
    @Operation(summary = "Excluir conta do usuário",
            description = "Inativa e anonimiza os dados do usuário. Exige confirmação de senha.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Senha incorreta ou não autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deleteAccount(
            @Valid @RequestBody DeleteAccountRequestDTO deleteRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UserNotFoundException("Nenhum usuário autenticado encontrado.");
        }

        Usuario usuario = userService.findUserById(userDetails.getUserId(), userDetails.getRole());
        if (usuario == null) {
            throw new UserNotFoundException("Usuário não encontrado.");
        }

        userService.deleteAccount(deleteRequest.getPassword(), usuario);

        return ResponseEntity.noContent().build();
    }
}

