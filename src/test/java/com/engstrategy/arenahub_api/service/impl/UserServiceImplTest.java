package com.engstrategy.arenahub_api.service.impl;

import com.engstrategy.arenahub_api.exceptions.InvalidPasswordException;
import com.engstrategy.arenahub_api.model.Agendamento;
import com.engstrategy.arenahub_api.model.Arena;
import com.engstrategy.arenahub_api.model.Atleta;
import com.engstrategy.arenahub_api.model.Endereco;
import com.engstrategy.arenahub_api.repository.ArenaRepository;
import com.engstrategy.arenahub_api.repository.AtletaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private ArenaRepository arenaRepository;

    @Mock
    private AtletaRepository atletaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Atleta atleta;
    private Arena arena;
    private String rawPassword = "password123";
    private String encodedPassword = "encodedPassword123";

    @BeforeEach
    void setUp() {
        atleta = Atleta.builder()
                .id(UUID.randomUUID())
                .nome("Atleta Teste")
                .email("atleta@teste.com")
                .telefone("(11) 99999-9999")
                .senha(encodedPassword)
                .cpfCnpj("123.456.789-00")
                .ativo(true)
                .agendamentos(new ArrayList<>())
                .build();

        arena = Arena.builder()
                .id(UUID.randomUUID())
                .nome("Arena Teste")
                .email("arena@teste.com")
                .telefone("(11) 88888-8888")
                .senha(encodedPassword)
                .cpfCnpj("12.345.678/0001-90")
                .cpfProprietario("123.456.789-11")
                .cnpj("12.345.678/0001-90")
                .chavePix("chave-pix")
                .endereco(Endereco.builder()
                        .rua("Rua Teste")
                        .numero("123")
                        .cidade("Cidade Teste")
                        .estado("ST")
                        .cep("12345-678")
                        .build())
                .ativo(true)
                .build();
    }

    @Test
    void deleteAccount_Atleta_Success() {
        Agendamento agendamento = Agendamento.builder()
                .id(1L)
                .nomePagadorPix("Atleta Teste")
                .telefonePagadorPix("(11) 99999-9999")
                .atleta(atleta)
                .build();
        atleta.getAgendamentos().add(agendamento);

        // Mocking the reload
        when(atletaRepository.findById(atleta.getId())).thenReturn(Optional.of(atleta));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        userService.deleteAccount(rawPassword, atleta);

        assertEquals("Conta deletada", atleta.getNome());
        assertTrue(atleta.getEmail().startsWith("deleted_"));
        assertTrue(atleta.getTelefone().startsWith("deleted_"));
        assertNull(atleta.getSenha());
        assertFalse(atleta.isAtivo());
        
        // Verificar agendamento anonimizado
        assertEquals("Conta deletada", agendamento.getNomePagadorPix());
        assertTrue(agendamento.getTelefonePagadorPix().startsWith("deleted_"));
        
        verify(atletaRepository, times(1)).save(atleta);
    }

    @Test
    void deleteAccount_Arena_Success() {
        // Mocking the reload
        when(arenaRepository.findById(arena.getId())).thenReturn(Optional.of(arena));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        userService.deleteAccount(rawPassword, arena);

        assertEquals("Conta deletada", arena.getNome());
        assertTrue(arena.getEmail().startsWith("deleted_"));
        assertTrue(arena.getCpfProprietario().startsWith("deleted_"));
        assertNull(arena.getChavePix());
        assertNull(arena.getSenha());
        assertEquals("EXCLUÍDO", arena.getEndereco().getRua());
        assertFalse(arena.isAtivo());
        verify(arenaRepository, times(1)).save(arena);
    }

    @Test
    void deleteAccount_InvalidPassword_ThrowsException() {
        // Mocking the reload
        when(atletaRepository.findById(atleta.getId())).thenReturn(Optional.of(atleta));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.deleteAccount("wrongPassword", atleta));
        verify(atletaRepository, never()).save(any());
    }
}
