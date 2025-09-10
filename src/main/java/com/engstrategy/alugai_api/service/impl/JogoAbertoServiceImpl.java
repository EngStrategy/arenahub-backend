package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.jogosabertos.JogoAbertoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.MinhaParticipacaoResponseDTO;
import com.engstrategy.alugai_api.dto.jogosabertos.SolicitacaoEntradaDTO;
import com.engstrategy.alugai_api.exceptions.AccessDeniedException;
import com.engstrategy.alugai_api.mapper.JogoAbertoMapper;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.SolicitacaoEntrada;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusSolicitacao;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.repository.SolicitacaoEntradaRepository;
import com.engstrategy.alugai_api.repository.specs.AgendamentoSpecs;
import com.engstrategy.alugai_api.service.JogoAbertoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JogoAbertoServiceImpl implements JogoAbertoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AtletaRepository atletaRepository;
    private final SolicitacaoEntradaRepository solicitacaoRepository;
    private final JogoAbertoMapper jogoAbertoMapper;
    private final EmailService emailService;

    @Override
    public Page<JogoAbertoResponseDTO> listarJogosAbertos(Pageable pageable, String cidade, String esporte, Double latitude, Double longitude, Double raioKm) {
        Page<Agendamento> jogosAbertosPage;

        if (latitude != null && longitude != null && raioKm != null && raioKm > 0) {
            // --- Busca por Proximidade ---
            Pageable proximityPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            jogosAbertosPage = agendamentoRepository.findJogosAbertosByProximity(latitude, longitude, raioKm, proximityPageable);

        } else {
            // --- Busca por Filtros Tradicionais ---
            Specification<Agendamento> spec = AgendamentoSpecs.isPublico()
                    .and(AgendamentoSpecs.isPendente())
                    .and(AgendamentoSpecs.hasVagas())
                    .and(AgendamentoSpecs.isUpcoming());

            if (cidade != null && !cidade.trim().isEmpty()) {
                spec = spec.and(AgendamentoSpecs.hasCidade(cidade));
            }
            if (esporte != null && !esporte.trim().isEmpty()) {
                spec = spec.and(AgendamentoSpecs.hasEsporte(esporte));
            }
            jogosAbertosPage = agendamentoRepository.findAll(spec, pageable);
        }

        return jogosAbertosPage.map(jogoAbertoMapper::toJogoAbertoResponseDTO);
    }

    @Override
    public SolicitacaoEntradaDTO solicitarEntrada(Long agendamentoId, UUID atletaId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Jogo aberto não encontrado."));

        ZoneId fusoHorarioBrasilia = ZoneId.of("America/Sao_Paulo");
        LocalDateTime dataHoraDoJogo = LocalDateTime.of(agendamento.getDataAgendamento(), agendamento.getHorarioInicio());
        if (LocalDateTime.now(fusoHorarioBrasilia).isAfter(dataHoraDoJogo.minusMinutes(30L))) {
            throw new IllegalStateException("Não é possível solicitar entrada em um jogo que começa em menos de 30 minutos.");
        }

        if (!agendamento.isPublico() || agendamento.getVagasDisponiveis() <= 0) {
            throw new IllegalArgumentException("Este jogo não está mais aberto a solicitações.");
        }

        if (agendamento.getAtleta().getId().equals(atletaId)) {
            throw new IllegalArgumentException("Você não pode solicitar entrada no seu próprio jogo.");
        }

        solicitacaoRepository.findByAgendamentoIdAndSolicitanteId(agendamentoId, atletaId)
                .ifPresent(s -> {
                    throw new IllegalStateException("Você já solicitou entrada neste jogo.");
                });

        Atleta solicitante = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado."));

        SolicitacaoEntrada solicitacao = new SolicitacaoEntrada();
        solicitacao.setAgendamento(agendamento);
        solicitacao.setSolicitante(solicitante);
        solicitacao.setStatus(StatusSolicitacao.PENDENTE);

        SolicitacaoEntrada savedSolicitacao = solicitacaoRepository.save(solicitacao);

        Atleta donoDoJogo = agendamento.getAtleta();
        emailService.enviarEmailNovaSolicitacao(donoDoJogo.getEmail(), donoDoJogo.getNome(), solicitante.getNome(), agendamento);
        return jogoAbertoMapper.toSolicitacaoEntradaDTO(savedSolicitacao);
    }

    @Override
    public List<SolicitacaoEntradaDTO> listarSolicitacoes(Long agendamentoId, UUID proprietarioId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));

        if (!agendamento.getAtleta().getId().equals(proprietarioId)) {
            throw new AccessDeniedException("Você não tem permissão para ver as solicitações deste jogo.");
        }

        return solicitacaoRepository.findByAgendamentoId(agendamentoId).stream()
                .map(jogoAbertoMapper::toSolicitacaoEntradaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SolicitacaoEntradaDTO gerenciarSolicitacao(Long solicitacaoId, UUID proprietarioId, boolean aceitar) {
        SolicitacaoEntrada solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));

        Agendamento agendamento = solicitacao.getAgendamento();

        if (!agendamento.getAtleta().getId().equals(proprietarioId)) {
            throw new AccessDeniedException("Você não tem permissão para gerenciar esta solicitação.");
        }

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Esta solicitação já foi respondida.");
        }

        if (aceitar) {
            if (agendamento.getVagasDisponiveis() <= 0) {
                throw new IllegalStateException("Não há mais vagas disponíveis neste jogo.");
            }
            solicitacao.setStatus(StatusSolicitacao.ACEITO);
            agendamento.getParticipantes().add(solicitacao.getSolicitante());
            agendamento.setVagasDisponiveis(agendamento.getVagasDisponiveis() - 1);
            emailService.enviarEmailSolicitacaoAceita(solicitacao.getSolicitante().getEmail(), solicitacao.getSolicitante().getNome(), agendamento);
        } else {
            solicitacao.setStatus(StatusSolicitacao.RECUSADO);
            emailService.enviarEmailSolicitacaoRecusada(solicitacao.getSolicitante().getEmail(), solicitacao.getSolicitante().getNome(), agendamento);
        }

        solicitacaoRepository.save(solicitacao);
        agendamentoRepository.save(agendamento);

        return jogoAbertoMapper.toSolicitacaoEntradaDTO(solicitacao);
    }

    @Override
    public void sairDeJogoAberto(Long solicitacaoId, UUID atletaId) {
        SolicitacaoEntrada solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada."));

        Agendamento agendamento = solicitacao.getAgendamento();

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Você não pode sair de um jogo que já foi cancelado");
        }

        ZoneId fusoHorarioBrasilia = ZoneId.of("America/Sao_Paulo");
        LocalDateTime dataHoraDoJogo = LocalDateTime.of(agendamento.getDataAgendamento(), agendamento.getHorarioInicio());
        if (LocalDateTime.now(fusoHorarioBrasilia).isAfter(dataHoraDoJogo.minusHours(3))) {
            throw new IllegalStateException("Você não pode sair de um jogo com menos de 2 horas de antecedência.");
        }

        if (!solicitacao.getSolicitante().getId().equals(atletaId)) {
            throw new AccessDeniedException("Você não tem permissão para cancelar esta participação.");
        }

        StatusSolicitacao statusAtual = solicitacao.getStatus();

        if (statusAtual == StatusSolicitacao.ACEITO) {
            // Se o atleta foi aceito, remove ele dos participantes e incrementa as vagas
            agendamento.getParticipantes().remove(solicitacao.getSolicitante());
            agendamento.setVagasDisponiveis(agendamento.getVagasDisponiveis() + 1);
            agendamentoRepository.save(agendamento);

            // Notifica o dono do jogo que um participante saiu
            Atleta donoDoJogo = agendamento.getAtleta();
            emailService.enviarEmailParticipanteSaiu(donoDoJogo.getEmail(), donoDoJogo.getNome(), solicitacao.getSolicitante().getNome(), agendamento);
        } else if (statusAtual != StatusSolicitacao.PENDENTE) {
            // Se a solicitação não estiver ACEITA nem PENDENTE, lança uma exceção
            throw new IllegalStateException("Sua participação precisa estar 'Aceita' ou 'Pendente' para que você possa sair.");
        }

        // Deleta a solicitação em ambos os casos (ACEITO ou PENDENTE)
        solicitacaoRepository.delete(solicitacao);
    }

    @Override
    @Transactional
    public List<MinhaParticipacaoResponseDTO> listarMinhasParticipacoes(UUID atletaId) {
        List<SolicitacaoEntrada> solicitacoes = solicitacaoRepository.findBySolicitanteIdOrderByAgendamentoDataAgendamentoDesc(atletaId);
        return solicitacoes.stream()
                .map(jogoAbertoMapper::toMinhaParticipacaoResponseDTO)
                .collect(Collectors.toList());
    }
}
