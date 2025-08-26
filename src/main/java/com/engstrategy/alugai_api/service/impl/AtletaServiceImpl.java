package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.atleta.AtletaBuscaResponseDTO;
import com.engstrategy.alugai_api.dto.atleta.AtletaUpdateDTO;
import com.engstrategy.alugai_api.exceptions.ExternalAccountExistsException;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.CodigoVerificacao;
import com.engstrategy.alugai_api.model.CodigoVerificacaoSms;
import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.TipoContaAtleta;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.repository.CodigoVerificacaoRepository;
import com.engstrategy.alugai_api.repository.CodigoVerificacaoSmsRepository;
import com.engstrategy.alugai_api.service.AtletaService;
import com.engstrategy.alugai_api.util.GeradorCodigoVerificacao;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtletaServiceImpl implements AtletaService {

    private final AtletaRepository atletaRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final EmailService emailService;
    private final UserServiceImpl userServiceImpl;
    private final CodigoVerificacaoSmsRepository codigoSmsRepository;
    private final SmsService smsService;

    @Override
    @Transactional
    public Atleta criarAtleta(Atleta atleta) {
        validarDadosUnicos(atleta.getEmail(), atleta.getTelefone());

        encodePassword(atleta);
        Atleta savedAtleta = atletaRepository.save(atleta);

        CodigoVerificacao codigoVerificacao = GeradorCodigoVerificacao.gerarCodigoVerificacao(savedAtleta.getEmail());
        codigoVerificacaoRepository.save(codigoVerificacao);

        emailService.enviarCodigoVerificacao(atleta.getEmail(), atleta.getNome(), codigoVerificacao.getCode());

        return savedAtleta;
    }

    @Override
    public Atleta buscarPorId(Long id) {
        return atletaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + id));
    }

    @Override
    public Page<Atleta> listarTodos(Pageable pageable) {
        return atletaRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Atleta atualizar(Long id, AtletaUpdateDTO atletaUpdateDTO) {
        Atleta savedAtleta = atletaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + id));

        // Verifica se o telefone mudou e se é único
        if (atletaUpdateDTO.getTelefone() != null && !atletaUpdateDTO.getTelefone().equals(savedAtleta.getTelefone())) {
            if (atletaRepository.existsByTelefone(atletaUpdateDTO.getTelefone())) {
                throw new UniqueConstraintViolationException("Telefone já está em uso.");
            }
            savedAtleta.setTelefone(atletaUpdateDTO.getTelefone());
        }

        // Atualiza os campos que não são nulos
        if (atletaUpdateDTO.getNome() != null) {
            savedAtleta.setNome(atletaUpdateDTO.getNome());
        }
        if (atletaUpdateDTO.getUrlFoto() != null) {
            savedAtleta.setUrlFoto(atletaUpdateDTO.getUrlFoto());
        }
        if (atletaUpdateDTO.getUrlFoto() == null) {
            savedAtleta.setUrlFoto(null);
        }

        return atletaRepository.save(savedAtleta);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + id));
        atletaRepository.delete(atleta);
    }

    private void validarDadosUnicos(String email, String telefone) {
        if (userServiceImpl.existsByEmail(email)) {
            throw new UniqueConstraintViolationException("Email já está em uso.");
        }

        Optional<Atleta> atletaExistente = atletaRepository.findAtletaByTelefone(telefone);

        if (atletaExistente.isPresent()) {
            Atleta atleta = atletaExistente.get();
            if (atleta.getTipoConta() == TipoContaAtleta.COMPLETO) {
                // Se a conta já é completa, é um erro de duplicidade normal.
                throw new UniqueConstraintViolationException("Telefone já está em uso.");
            } else {
                // Se a conta é EXTERNA, lançamos nossa nova exceção para o frontend saber que deve redirecionar.
                throw new ExternalAccountExistsException("Já existe uma conta externa com este telefone. Por favor, ative sua conta para continuar.");
            }
        }
    }

    private void encodePassword(Atleta atleta) {
        String encodedPassword = passwordEncoder.encode(atleta.getSenha());
        atleta.setSenha(encodedPassword);
    }

    @Override
    @Transactional
    public void redefinirSenha(Usuario usuario, String novaSenha) {
        if (!(usuario instanceof Atleta)) {
            throw new IllegalArgumentException("Usuário não é um Atleta");
        }
        Atleta atleta = (Atleta) usuario;
        atleta.setSenha(passwordEncoder.encode(novaSenha));
        atletaRepository.save(atleta);
    }

    @Override
    @Transactional
    public void alterarSenha(Long atletaId, String senhaAtual, String novaSenha) {
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado"));

        if (!passwordEncoder.matches(senhaAtual, atleta.getSenha())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        atleta.setSenha(passwordEncoder.encode(novaSenha));
        atletaRepository.save(atleta);
    }

    @Override
    public List<AtletaBuscaResponseDTO> buscarPorNomeOuTelefone(String query) {
        String telefoneQuery = query.replaceAll("\\D", "");

        // Chama o método de busca do repositório
        List<Atleta> atletas = atletaRepository.searchByNomeOuTelefone(query, telefoneQuery);

        return atletas.stream()
                .map(atleta -> AtletaBuscaResponseDTO.builder()
                        .id(atleta.getId())
                        .nome(atleta.getNome())
                        .telefone(atleta.getTelefone())
                        .urlFoto(atleta.getUrlFoto())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void iniciarAtivacaoConta(String telefone) {
        System.out.println("Telefone recebido: " + telefone);
        Atleta atleta = atletaRepository.findAtletaByTelefone(telefone)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum atleta externo encontrado com este telefone."));

        if (atleta.getTipoConta() == TipoContaAtleta.COMPLETO) {
            throw new IllegalStateException("Esta conta já está ativa.");
        }

        Optional<CodigoVerificacaoSms> codigoExistenteOpt = codigoSmsRepository.findByAtleta_Id(atleta.getId());

        // Gera um novo código aleatório de 6 dígitos
        String novoCodigo = String.format("%06d", new java.util.Random().nextInt(999999));

        if (codigoExistenteOpt.isPresent()) {
            // Se já existe um código, atualiza ele
            CodigoVerificacaoSms codigoExistente = codigoExistenteOpt.get();

            // Cooldown de 60 segundos para reenvio
            long segundosDesdeUltimoEnvio = ChronoUnit.SECONDS.between(codigoExistente.getDataExpiracao().minusMinutes(10), LocalDateTime.now());
            if (segundosDesdeUltimoEnvio < 60) {
                throw new IllegalStateException("Aguarde " + (60 - segundosDesdeUltimoEnvio) + " segundos para solicitar um novo código.");
            }

            codigoExistente.setCodigo(novoCodigo);
            codigoExistente.setDataExpiracao(LocalDateTime.now().plusMinutes(10)); // Atualiza a validade
            codigoSmsRepository.save(codigoExistente);

        } else {
            // Se não existe, cria um novo
            CodigoVerificacaoSms codigoSms = CodigoVerificacaoSms.builder()
                    .atleta(atleta)
                    .codigo(novoCodigo)
                    .dataExpiracao(LocalDateTime.now().plusMinutes(10))
                    .build();
            codigoSmsRepository.save(codigoSms);
        }

        // Envia o SMS com o novo código
        String mensagem = "Seu código de ativação ArenaHub é: " + novoCodigo;
        smsService.enviarSms(atleta.getTelefone(), mensagem);
    }

    @Transactional
    public void ativarConta(String telefone, String codigo, String email, String senha) {
        // Valida o código
        CodigoVerificacaoSms codigoSms = codigoSmsRepository.findByAtleta_TelefoneAndCodigo(telefone, codigo)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificação inválido."));

        if (codigoSms.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código de verificação expirado.");
        }

        // Atualiza os dados do atleta
        Atleta atleta = codigoSms.getAtleta();
        atleta.setEmail(email); // Valide se o email já não existe
        atleta.setSenha(passwordEncoder.encode(senha));
        atleta.setTipoConta(TipoContaAtleta.COMPLETO);

        atletaRepository.save(atleta);

        // Deleta o código usado
        codigoSmsRepository.delete(codigoSms);
    }
}