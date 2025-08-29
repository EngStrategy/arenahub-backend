package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.atleta.AtletaBuscaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AtletaService {
    Atleta criarAtleta(Atleta atleta);
    Atleta buscarPorId(UUID id);
    Page<Atleta> listarTodos(Pageable pageable);
    Atleta atualizar(UUID id, AtletaUpdateDTO atletaUpdateDTO);
    void excluir(UUID id);
    void redefinirSenha(Usuario usuario, String novaSenha);
    void alterarSenha(UUID atletaId, String senhaAtual, String novaSenha);
    List<AtletaBuscaResponseDTO> buscarPorNomeOuTelefone(String query);
    void iniciarAtivacaoConta(String telefone);
    void ativarConta(String telefone, String codigo, String email, String senha);
}
