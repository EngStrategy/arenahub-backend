package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.atleta.AtletaBuscaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AtletaService {
    Atleta criarAtleta(Atleta atleta);
    Atleta buscarPorId(Long id);
    Page<Atleta> listarTodos(Pageable pageable);
    Atleta atualizar(Long id, AtletaUpdateDTO atletaUpdateDTO);
    void excluir(Long id);
    void redefinirSenha(Usuario usuario, String novaSenha);
    void alterarSenha(Long atletaId, String senhaAtual, String novaSenha);
    List<AtletaBuscaResponseDTO> buscarPorNomeOuTelefone(String query);
    void iniciarAtivacaoConta(String telefone);
    void ativarConta(String telefone, String codigo, String email, String senha);
}
