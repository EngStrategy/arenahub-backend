package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoDTO;
import com.engstrategy.alugai_api.dto.avaliacao.AvaliacaoResponseDTO;
import com.engstrategy.alugai_api.exceptions.AccessDeniedException;
import com.engstrategy.alugai_api.jwt.CustomUserDetails;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.Avaliacao;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.repository.AvaliacaoRepository;
import com.engstrategy.alugai_api.service.AvaliacaoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvaliacaoServiceImpl implements AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ZoneId fusoHorarioPadrao = ZoneId.of("America/Sao_Paulo");

    @Override
    @Transactional
    public Optional<AvaliacaoResponseDTO> criarOuDispensarAvaliacao(Long agendamentoId, AvaliacaoDTO avaliacaoCreateDTO, CustomUserDetails userDetails) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + agendamentoId));

        if (!agendamento.getAtleta().getId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Você só pode interagir com os seus próprios agendamentos.");
        }
        if (agendamento.getAvaliacao() != null) {
            throw new IllegalStateException("Este agendamento já foi avaliado.");
        }
        if (Boolean.TRUE.equals(agendamento.getAvaliacaoDispensada())) {
            throw new IllegalStateException("A avaliação para este agendamento já foi dispensada.");
        }

        LocalDateTime dataHoraFimAgendamento = agendamento.getDataAgendamento().atTime(agendamento.getHorarioFimSnapshot());
        if (dataHoraFimAgendamento.isAfter(LocalDateTime.now(fusoHorarioPadrao))) {
            throw new IllegalStateException("Você só pode avaliar um agendamento após a sua conclusão.");
        }

        if (avaliacaoCreateDTO.getNota() != null) {
            // Usuário enviou uma nota (está criando uma avaliação)
            Avaliacao novaAvaliacao = Avaliacao.builder()
                    .nota(avaliacaoCreateDTO.getNota())
                    .comentario(avaliacaoCreateDTO.getComentario())
                    .agendamento(agendamento)
                    .atleta(agendamento.getAtleta())
                    .build();
            Avaliacao avaliacaoSalva = avaliacaoRepository.save(novaAvaliacao);
            return Optional.of(mapToResponseDTO(avaliacaoSalva)); // Retorna a avaliação criada

        } else {
            agendamento.setAvaliacaoDispensada(true);
            agendamentoRepository.save(agendamento);
            return Optional.empty(); // Retorna um Optional vazio para indicar que nenhuma avaliação foi criada
        }
    }

    @Override
    @Transactional
    public AvaliacaoResponseDTO atualizarAvaliacao(Long avaliacaoId, AvaliacaoDTO avaliacaoDTO, CustomUserDetails userDetails) {
        // Encontra a avaliação existente ou lança uma exceção
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada com o ID: " + avaliacaoId));

        // Validação de segurança: o usuário logado é o dono da avaliação?
        if (!avaliacao.getAtleta().getId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException("Você só pode atualizar suas próprias avaliações.");
        }

        // Validação da regra de negócio: a avaliação foi criada há menos de 48 horas?
        LocalDateTime dataCriacao = avaliacao.getDataAvaliacao();
        LocalDateTime agora = LocalDateTime.now(fusoHorarioPadrao);
        long horasDesdeCriacao = ChronoUnit.HOURS.between(dataCriacao, agora);

        if (horasDesdeCriacao > 48) {
            throw new IllegalStateException("A avaliação só pode ser alterada em até 48 horas após a sua criação.");
        }

        avaliacao.setNota(avaliacaoDTO.getNota());
        avaliacao.setComentario(avaliacaoDTO.getComentario());

        // Salva a entidade atualizada no banco de dados
        Avaliacao avaliacaoAtualizada = avaliacaoRepository.save(avaliacao);

        // Mapeia para o DTO de resposta e retorna
        return mapToResponseDTO(avaliacaoAtualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDTO> buscarAvaliacoesPorQuadra(Pageable pageable, Long quadraId) {
        Page<Avaliacao> avaliacoesPage = avaliacaoRepository.findByAgendamento_Quadra_IdOrderByDataAvaliacaoDesc(quadraId, pageable);

        return avaliacoesPage.map(this::mapToResponseDTO);
    }

    private AvaliacaoResponseDTO mapToResponseDTO(Avaliacao avaliacao) {
        Atleta atleta = avaliacao.getAtleta();
        return AvaliacaoResponseDTO.builder()
                .id(avaliacao.getId())
                .nota(avaliacao.getNota())
                .comentario(avaliacao.getComentario())
                .dataAvaliacao(avaliacao.getDataAvaliacao())
                .nomeAtleta(atleta.getNome())
                .urlFotoAtleta(atleta.getUrlFoto())
                .build();
    }
}