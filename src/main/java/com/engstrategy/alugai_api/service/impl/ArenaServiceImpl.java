package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoDashboardDTO;
import com.engstrategy.alugai_api.dto.agendamento.arena.CidadeDTO;
import com.engstrategy.alugai_api.dto.arena.*;
import com.engstrategy.alugai_api.exceptions.SubscriptionInactiveException;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.mapper.ArenaMapper;
import com.engstrategy.alugai_api.mapper.EnderecoMapper;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.StatusAssinatura;
import com.engstrategy.alugai_api.repository.*;
import com.engstrategy.alugai_api.repository.specs.ArenaSpecs;
import com.engstrategy.alugai_api.service.ArenaService;
import com.engstrategy.alugai_api.util.GeradorCodigoVerificacao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArenaServiceImpl implements ArenaService {

    private final ArenaRepository arenaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnderecoMapper enderecoMapper;
    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final EmailService emailService;
    private final UserServiceImpl userServiceImpl;
    private final AgendamentoRepository agendamentoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ArenaMapper arenaMapper;

    private void verificarAssinaturaAtiva(UUID arenaId) {
        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada."));

        if (arena.getStatusAssinatura() != StatusAssinatura.ATIVA) {
            throw new SubscriptionInactiveException("Sua assinatura não está ativa. Por favor, regularize seu plano.");
        }
    }

    @Override
    @Transactional
    public Arena criarArena(Arena arena) {
        validarDadosUnicos(arena.getEmail(), arena.getTelefone(),
                arena.getCpfProprietario(), arena.getCnpj());

        arena.setStatusAssinatura(StatusAssinatura.INATIVA);

        encodePassword(arena);
        Arena savedArena = arenaRepository.save(arena);

        CodigoVerificacao codigoVerificacao = GeradorCodigoVerificacao.gerarCodigoVerificacao(savedArena.getEmail());
        codigoVerificacaoRepository.save(codigoVerificacao);

        emailService.enviarCodigoVerificacao(arena.getEmail(), arena.getNome(), codigoVerificacao.getCode());

        return savedArena;
    }

    @Override
    @Transactional(readOnly = true)
    public ArenaResponseDTO buscarPorId(UUID id) {
        Arena arena = arenaRepository.findByIdWithQuadras(id)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));

        ArenaRatingInfo ratingInfo = avaliacaoRepository.findArenaRatingInfoByArenaId(id);
        ArenaResponseDTO responseDTO = arenaMapper.mapArenaToArenaResponseDTO(arena);

        if (ratingInfo != null && ratingInfo.getQuantidadeAvaliacoes() > 0) {
            responseDTO.setNotaMedia(ratingInfo.getNotaMedia());
            responseDTO.setQuantidadeAvaliacoes(ratingInfo.getQuantidadeAvaliacoes());
        } else {
            responseDTO.setNotaMedia(0.0);
            responseDTO.setQuantidadeAvaliacoes(0L);
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArenaResponseDTO> listarTodos(Pageable pageable, String cidade, String esporte, Double latitude, Double longitude, Double raioKm) {

        // Esta variável guarda o resultado, seja da busca por proximidade ou por filtros.
        Page<Arena> arenasPage;

        // Decide qual busca será executada com base nos parâmetros recebidos.
        if (latitude != null && longitude != null && raioKm != null && raioKm > 0) {

            // --- Caminho A: Busca por Proximidade ---
            // Na busca por proximidade, a ordenação já é por distância, então não uso o 'sort' do request.
            Pageable proximityPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            arenasPage = arenaRepository.findByProximity(latitude, longitude, raioKm, proximityPageable);

        } else {

            // --- Caminho B: Busca por Filtros Tradicionais ---
            Specification<Arena> spec = ArenaSpecs.isAtivo()
                    .and(ArenaSpecs.isAssinaturaAtiva());
            if (cidade != null && !cidade.trim().isEmpty()) {
                spec = spec.and(ArenaSpecs.hasCidade(cidade));
            }
            if (esporte != null && !esporte.trim().isEmpty()) {
                spec = spec.and(ArenaSpecs.hasEsporte(esporte));
            }
            arenasPage = arenaRepository.findAll(spec, pageable);
        }

        if (arenasPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Pega os IDs das arenas da página atual
        List<UUID> arenaIds = arenasPage.getContent().stream()
                .map(Arena::getId)
                .collect(Collectors.toList());

        // Busca as informações de avaliação para todas as arenas da página em UMA ÚNICA QUERY
        List<Map<String, Object>> ratings = avaliacaoRepository.findArenaRatingInfoForArenas(arenaIds);
        Map<UUID, ArenaRatingInfo> ratingsMap = ratings.stream()
                .collect(Collectors.toMap(
                        r -> (UUID) r.get("arenaId"),
                        r -> new ArenaRatingInfo((Double) r.get("notaMedia"), (Long) r.get("quantidadeAvaliacoes"))
                ));

        return arenasPage.map(arena -> {
            ArenaResponseDTO dto = arenaMapper.mapArenaToArenaResponseDTO(arena);
            ArenaRatingInfo ratingInfo = ratingsMap.get(arena.getId());

            if (ratingInfo != null) {
                dto.setNotaMedia(ratingInfo.getNotaMedia());
                dto.setQuantidadeAvaliacoes(ratingInfo.getQuantidadeAvaliacoes());
            } else {
                dto.setNotaMedia(0.0);
                dto.setQuantidadeAvaliacoes(0L);
            }
            return dto;
        });
    }

    @Override
    @Transactional
    public ArenaResponseDTO atualizar(UUID id, ArenaUpdateDTO arenaUpdateDTO) {
        verificarAssinaturaAtiva(id);

        Arena savedArena = arenaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));

        if (arenaUpdateDTO.getTelefone() != null && !arenaUpdateDTO.getTelefone().equals(savedArena.getTelefone())) {
            if (arenaRepository.existsByTelefone(arenaUpdateDTO.getTelefone())) {
                throw new UniqueConstraintViolationException("Telefone já está em uso.");
            }
            savedArena.setTelefone(arenaUpdateDTO.getTelefone());
        }

        if (arenaUpdateDTO.getNome() != null) {
            savedArena.setNome(arenaUpdateDTO.getNome());
        }
        if (arenaUpdateDTO.getDescricao() != null) {
            savedArena.setDescricao(arenaUpdateDTO.getDescricao());
        }
        if (arenaUpdateDTO.getUrlFoto() != null) {
            savedArena.setUrlFoto(arenaUpdateDTO.getUrlFoto());
        }
        if (arenaUpdateDTO.getEndereco() != null) {
            savedArena.setEndereco(enderecoMapper.mapEnderecoDtoToEndereco(arenaUpdateDTO.getEndereco()));
        }
        if (arenaUpdateDTO.getUrlFoto() == null) {
            savedArena.setUrlFoto(null);
        }

        if (savedArena.getQuadras() != null) {
            savedArena.getQuadras().size();
        }

        Arena updatedArena = arenaRepository.save(savedArena);

        return arenaMapper.mapArenaToArenaResponseDTO(updatedArena);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));
        arenaRepository.delete(arena);
    }

    private void validarDadosUnicos(String email, String telefone, String cpfProprietario, String cnpj) {
        if (userServiceImpl.existsByEmail(email)) {
            throw new UniqueConstraintViolationException("Email já está em uso.");
        }
        if (userServiceImpl.existsByTelefone(telefone)) {
            throw new UniqueConstraintViolationException("Telefone já está em uso.");
        }
        if (arenaRepository.existsByCpfProprietario(cpfProprietario)) {
            throw new UniqueConstraintViolationException("CPF do proprietário já está em uso.");
        }
        if (cnpj != null && arenaRepository.existsByCnpj(cnpj)) {
            throw new UniqueConstraintViolationException("CNPJ já está em uso.");
        }
    }

    private void encodePassword(Arena arena) {
        String encodedPassword = passwordEncoder.encode(arena.getSenha());
        arena.setSenha(encodedPassword);
    }

    @Override
    @Transactional
    public void redefinirSenha(Usuario usuario, String novaSenha) {
        if (!(usuario instanceof Arena)) {
            throw new IllegalArgumentException("Usuário não é uma Arena");
        }
        Arena arena = (Arena) usuario;
        arena.setSenha(passwordEncoder.encode(novaSenha));
        arenaRepository.save(arena);
    }

    @Override
    @Transactional
    public void alterarSenha(UUID arenaId, String senhaAtual, String novaSenha) {
        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada"));

        if (!passwordEncoder.matches(senhaAtual, arena.getSenha())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        arena.setSenha(passwordEncoder.encode(novaSenha));
        arenaRepository.save(arena);
    }

    @Override
    public List<CidadeDTO> getCidades() {
        return arenaRepository.findDistinctCidadeAndEstado().stream()
                .map(result -> new CidadeDTO((String) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArenaDashboardDTO getDashboardData(UUID arenaId, Integer dias) {
        verificarAssinaturaAtiva(arenaId);

        Arena arena = arenaRepository.findByIdFetchingQuadrasAndHorarios(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com o ID: " + arenaId));

        // Definição de Fuso e Data Atual
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        LocalDateTime agora = LocalDateTime.now(zoneId);
        LocalDate hoje = agora.toLocalDate();
        LocalTime horaAtual = agora.toLocalTime();

        // 1. Definição do Período Atual e Anterior (Dinâmico)
        LocalDateTime fimPeriodoAtual = agora;
        LocalDateTime inicioPeriodoAtual;
        LocalDateTime inicioPeriodoAnterior;
        LocalDateTime fimPeriodoAnterior;

        if (dias == null || dias == 0) {
            // Lógica "Este Mês" (Padrão)
            inicioPeriodoAtual = hoje.withDayOfMonth(1).atStartOfDay();

            // Comparação: Mês anterior completo
            LocalDate mesAnterior = hoje.minusMonths(1);
            inicioPeriodoAnterior = mesAnterior.withDayOfMonth(1).atStartOfDay();
            fimPeriodoAnterior = mesAnterior.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        } else {
            // Lógica "Últimos X Dias"
            inicioPeriodoAtual = fimPeriodoAtual.minusDays(dias);

            // Comparação: O mesmo intervalo de X dias antes do atual
            fimPeriodoAnterior = inicioPeriodoAtual;
            inicioPeriodoAnterior = fimPeriodoAnterior.minusDays(dias);
        }

        // 2. Cálculo de Receita (Faturamento)
        BigDecimal receitaAtual = agendamentoRepository.calcularReceitaPorPeriodo(arenaId, inicioPeriodoAtual, fimPeriodoAtual);
        receitaAtual = (receitaAtual == null) ? BigDecimal.ZERO : receitaAtual;

        BigDecimal receitaAnterior = agendamentoRepository.calcularReceitaPorPeriodo(arenaId, inicioPeriodoAnterior, fimPeriodoAnterior);
        receitaAnterior = (receitaAnterior == null) ? BigDecimal.ZERO : receitaAnterior;

        Double percentualReceita = 0.0;
        if (receitaAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variacao = receitaAtual.subtract(receitaAnterior);
            percentualReceita = variacao.divide(receitaAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        } else if (receitaAtual.compareTo(BigDecimal.ZERO) > 0) {
            percentualReceita = 100.0;
        }

        // 3. Novos Clientes (Dinâmico)
        int novosClientesAtual = agendamentoRepository.countNovosClientesDaArenaPorPeriodo(arenaId, inicioPeriodoAtual, fimPeriodoAtual);
        int novosClientesAnterior = agendamentoRepository.countNovosClientesDaArenaPorPeriodo(arenaId, inicioPeriodoAnterior, fimPeriodoAnterior);
        int diferencaNovosClientes = novosClientesAtual - novosClientesAnterior;

        // 4. Gráfico de Reservas por Quadra (Dinâmico - Usa o período selecionado)
        List<QuadraEstatisticaDTO> statsQuadras = agendamentoRepository.countAgendamentosPorQuadra(arenaId, inicioPeriodoAtual, fimPeriodoAtual);

        // 5. Agendamentos de Hoje (Mantém fixo no dia de hoje para o card "Hoje")
        int agendamentosConfirmadosHoje = agendamentoRepository.countByArenaIdAndDataAgendamento(arenaId, hoje);

        // Lógica da Taxa de Ocupação (Focada em HOJE)
        Double taxaOcupacaoHoje = calcularTaxaOcupacaoHoje(arena, hoje, agendamentosConfirmadosHoje);

        // 6. Próximos Agendamentos (Focado em HOJE/FUTURO)
        List<Agendamento> proximosAgendamentos = agendamentoRepository.findProximosAgendamentosDoDia(arenaId, hoje, horaAtual);
        List<AgendamentoDashboardDTO> proximosAgendamentosDTO = proximosAgendamentos.stream()
                .map(agendamento -> AgendamentoDashboardDTO.builder()
                        .agendamentoId(agendamento.getId())
                        .clienteNome(agendamento.getAtleta().getNome())
                        .urlFoto(agendamento.getAtleta().getUrlFoto())
                        .quadraNome(agendamento.getQuadra().getNomeQuadra())
                        .horarioInicio(agendamento.getHorarioInicioSnapshot())
                        .horarioFim(agendamento.getHorarioFimSnapshot())
                        .clienteTelefone(agendamento.getAtleta().getTelefone())
                        .build())
                .toList();

        return ArenaDashboardDTO.builder()
                .nomeArena(arena.getNome())
                .receitaDoMes(receitaAtual)
                .percentualReceitaVsMesAnterior(percentualReceita)
                .agendamentosHoje(agendamentosConfirmadosHoje)
                .taxaOcupacaoHoje(taxaOcupacaoHoje)
                .novosClientes(novosClientesAtual)
                .diferencaNovosClientesVsSemanaAnterior(diferencaNovosClientes)
                .proximosAgendamentos(proximosAgendamentosDTO)
                .reservasPorQuadra(statsQuadras)
                .build();
    }

    private Double calcularTaxaOcupacaoHoje(Arena arena, LocalDate hoje, int agendamentosConfirmadosHoje) {
        int totalSlotsOperacionaisHoje = 0;
        DiaDaSemana diaDaSemanaHoje = DiaDaSemana.fromLocalDate(hoje);

        for (Quadra quadra : arena.getQuadras()) {
            if (quadra.getDuracaoReserva() == null || quadra.getDuracaoReserva().getMinutos() <= 0) continue;

            for (HorarioFuncionamento hf : quadra.getHorariosFuncionamento()) {
                if (hf.getDiaDaSemana() == diaDaSemanaHoje) {
                    for (IntervaloHorario intervalo : hf.getIntervalosDeHorario()) {
                        long duracaoIntervaloMinutos = Duration.between(intervalo.getInicio(), intervalo.getFim()).toMinutes();
                        if (intervalo.getFim().equals(LocalTime.of(23, 59))) duracaoIntervaloMinutos++;

                        totalSlotsOperacionaisHoje += (int) (duracaoIntervaloMinutos / quadra.getDuracaoReserva().getMinutos());
                    }
                    break;
                }
            }
        }

        if (totalSlotsOperacionaisHoje > 0) {
            return ((double) agendamentosConfirmadosHoje / totalSlotsOperacionaisHoje) * 100;
        }
        return 0.0;
    }
}
