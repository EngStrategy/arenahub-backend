package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoDashboardDTO;
import com.engstrategy.alugai_api.dto.agendamento.arena.CidadeDTO;
import com.engstrategy.alugai_api.dto.arena.*;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.mapper.ArenaMapper;
import com.engstrategy.alugai_api.mapper.EnderecoMapper;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
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

    @Override
    @Transactional
    public Arena criarArena(Arena arena) {
        validarDadosUnicos(arena.getEmail(), arena.getTelefone(),
                arena.getCpfProprietario(), arena.getCnpj());

        encodePassword(arena);
        Arena savedArena = arenaRepository.save(arena);

        CodigoVerificacao codigoVerificacao = GeradorCodigoVerificacao.gerarCodigoVerificacao(savedArena.getEmail());
        codigoVerificacaoRepository.save(codigoVerificacao);

        emailService.enviarCodigoVerificacao(arena.getEmail(), arena.getNome(), codigoVerificacao.getCode());

        return savedArena;
    }

    @Override
    public ArenaResponseDTO buscarPorId(UUID id) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + id));

        // Busca as informações de avaliação
        ArenaRatingInfo ratingInfo = avaliacaoRepository.findArenaRatingInfoByArenaId(id);

        // Mapeia a arena para o DTO
        ArenaResponseDTO responseDTO = arenaMapper.mapArenaToArenaResponseDTO(arena);

        // Adiciona as informações de avaliação ao DTO, se existirem
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
            Specification<Arena> spec = ArenaSpecs.isAtivo();
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
        Map<Long, ArenaRatingInfo> ratingsMap = ratings.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r.get("arenaId"),
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
    public Arena atualizar(UUID id, ArenaUpdateDTO arenaUpdateDTO) {
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

        return arenaRepository.save(savedArena);
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
    public ArenaDashboardDTO getDashboardData(UUID arenaId) {
        // Busca a arena com todas as suas dependências
        Arena arena = arenaRepository.findByIdFetchingQuadrasAndHorarios(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com o ID: " + arenaId));

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        LocalTime agora = LocalTime.now(ZoneId.of("America/Sao_Paulo"));
        DiaDaSemana diaDaSemanaHoje = DiaDaSemana.fromLocalDate(hoje);


        // CÁLCULO DE RECEITA MENSAL E VARIAÇÃO
        LocalDateTime inicioMesAtual = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimDoDiaDeHoje = hoje.atTime(LocalTime.MAX);
        BigDecimal receitaDoMes = agendamentoRepository.calcularReceitaPorPeriodo(arenaId, inicioMesAtual, fimDoDiaDeHoje);
        receitaDoMes = (receitaDoMes == null) ? BigDecimal.ZERO : receitaDoMes;

        // Calcula a receita do mês anterior completo
        LocalDate mesAnterior = hoje.minusMonths(1);
        LocalDateTime inicioMesAnterior = mesAnterior.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMesAnterior = mesAnterior.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        BigDecimal receitaMesAnterior = agendamentoRepository.calcularReceitaPorPeriodo(arenaId, inicioMesAnterior, fimMesAnterior);
        receitaMesAnterior = (receitaMesAnterior == null) ? BigDecimal.ZERO : receitaMesAnterior;

        Double percentualReceita;
        if (receitaMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variacao = receitaDoMes.subtract(receitaMesAnterior);
            BigDecimal percentual = variacao.divide(receitaMesAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            percentualReceita = percentual.doubleValue();
        } else if (receitaMesAnterior.compareTo(BigDecimal.ZERO) == 0 && receitaDoMes.compareTo(BigDecimal.ZERO) > 0) {
            percentualReceita = 100.0;
        } else {
            percentualReceita = 0.0;
        }

        int totalSlotsOperacionaisHoje = 0;
        for (Quadra quadra : arena.getQuadras()) {

            if (quadra.getDuracaoReserva() == null) {
                continue;
            }

            int duracaoReservaMinutos = quadra.getDuracaoReserva().getMinutos();

            if (duracaoReservaMinutos <= 0) {
                continue;
            }

            for (HorarioFuncionamento hf : quadra.getHorariosFuncionamento()) {
                if (hf.getDiaDaSemana() == diaDaSemanaHoje) {
                    for (IntervaloHorario intervalo : hf.getIntervalosDeHorario()) {
                        long duracaoIntervaloMinutos = Duration.between(intervalo.getInicio(), intervalo.getFim()).toMinutes();

                        if (intervalo.getFim().equals(LocalTime.of(23, 59))) {
                            duracaoIntervaloMinutos++;
                        }

                        int slotsNesteIntervalo = (int) (duracaoIntervaloMinutos / duracaoReservaMinutos);
                        totalSlotsOperacionaisHoje += slotsNesteIntervalo;
                    }
                    break;
                }
            }
        }

        // Busca o número de agendamentos já confirmados para hoje
        int agendamentosConfirmadosHoje = agendamentoRepository.countByArenaIdAndDataAgendamento(arenaId, hoje);

        // Lógica da Taxa de Ocupação
        Double taxaOcupacaoHoje = 0.0;
        if (totalSlotsOperacionaisHoje > 0) { // Proteção contra divisão por zero se a arena estiver
            taxaOcupacaoHoje = ((double) agendamentosConfirmadosHoje / totalSlotsOperacionaisHoje) * 100;
        }

        // CÁLCULO DE NOVOS CLIENTES NA SEMANA E VARIAÇÃO
        LocalDateTime inicioSemanaAtual = hoje.with(DayOfWeek.MONDAY).atStartOfDay();
        int novosClientesSemana = agendamentoRepository.countNovosClientesDaArenaPorPeriodo(arenaId, inicioSemanaAtual, fimDoDiaDeHoje);

        // Calcula novos clientes da semana anterior completa
        LocalDateTime inicioSemanaAnterior = inicioSemanaAtual.minusWeeks(1);
        LocalDateTime fimSemanaAnterior = inicioSemanaAnterior.plusDays(6).with(LocalTime.MAX);
        int novosClientesSemanaAnterior = agendamentoRepository.countNovosClientesDaArenaPorPeriodo(arenaId, inicioSemanaAnterior, fimSemanaAnterior);

        int diferencaNovosClientes = novosClientesSemana - novosClientesSemanaAnterior;


        // BUSCA DOS PRÓXIMOS AGENDAMENTOS DO DIA
        List<Agendamento> proximosAgendamentos = agendamentoRepository.findProximosAgendamentosDoDia(arenaId, hoje, agora);
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
                .receitaDoMes(receitaDoMes)
                .percentualReceitaVsMesAnterior(percentualReceita)
                .agendamentosHoje(agendamentosConfirmadosHoje)
                .taxaOcupacaoHoje(taxaOcupacaoHoje)
                .novosClientes(novosClientesSemana)
                .diferencaNovosClientesVsSemanaAnterior(diferencaNovosClientes)
                .proximosAgendamentos(proximosAgendamentosDTO)
                .build();
    }
}
