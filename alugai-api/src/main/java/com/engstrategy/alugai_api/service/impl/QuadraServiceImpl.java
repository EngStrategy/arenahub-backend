package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.quadra.HorarioFuncionamentoUpdateDTO;
import com.engstrategy.alugai_api.dto.quadra.IntervaloHorarioUpdateDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.dto.quadra.SlotHorarioResponseDTO;
import com.engstrategy.alugai_api.exceptions.*;
import com.engstrategy.alugai_api.mapper.QuadraMapper;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.StatusDisponibilidade;
import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import com.engstrategy.alugai_api.repository.*;
import com.engstrategy.alugai_api.service.QuadraService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuadraServiceImpl implements QuadraService {

    private final QuadraRepository quadraRepository;
    private final ArenaRepository arenaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final SlotHorarioService slotHorarioService;
    private final QuadraMapper quadraMapper;
    private final EntityManager entityManager;
    private final SlotHorarioRepository slotHorarioRepository;

    @Override
    @Transactional
    public Quadra criarQuadra(Quadra quadra, Long arenaId) {
        validarDadosUnicos(quadra.getNomeQuadra());
        validarHorariosFuncionamento(quadra.getHorariosFuncionamento());

        if (quadra.getHorariosFuncionamento().size() != 7) {
            throw new IllegalArgumentException("A quadra deve ter exatamente 7 horários de funcionamento, um para cada dia da semana.");
        }

        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + arenaId));

        quadra.getHorariosFuncionamento().forEach(horario -> horario.setQuadra(quadra));
        quadra.setArena(arena);

        slotHorarioService.gerarSlotsParaQuadra(quadra);

        return quadraRepository.save(quadra);
    }

    @Override
    @Transactional
    public Quadra atualizar(Long quadraId, QuadraUpdateDTO updateDTO, Long arenaId) {
        Quadra quadra = quadraRepository.findById(quadraId)
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + quadraId));

        if (!quadra.getArena().getId().equals(arenaId)) {
            throw new AccessDeniedException("Usuário não autorizado para atualizar esta quadra");
        }

        // Atualizar campos básicos da quadra
        atualizarCamposBasicos(quadra, updateDTO);

        // Atualizar horários de funcionamento se fornecidos
        if (updateDTO.getHorariosFuncionamento() != null) {
            atualizarHorariosFuncionamento(quadra, updateDTO.getHorariosFuncionamento());
        }

        return quadraRepository.save(quadra);
    }

    private void atualizarCamposBasicos(Quadra quadra, QuadraUpdateDTO updateDTO) {
        if (updateDTO.getNomeQuadra() != null && !updateDTO.getNomeQuadra().equals(quadra.getNomeQuadra())) {
            validarDadosUnicos(updateDTO.getNomeQuadra());
            quadra.setNomeQuadra(updateDTO.getNomeQuadra());
        }

        if (updateDTO.getUrlFotoQuadra() != null) {
            quadra.setUrlFotoQuadra(updateDTO.getUrlFotoQuadra());
        }

        if(updateDTO.getUrlFotoQuadra() == null) {
            quadra.setUrlFotoQuadra(null);
        }

        if (updateDTO.getTipoQuadra() != null) {
            quadra.setTipoQuadra(updateDTO.getTipoQuadra());
        }

        if (updateDTO.getDescricao() != null) {
            quadra.setDescricao(updateDTO.getDescricao());
        }

        if (updateDTO.getDuracaoReserva() != null) {
            quadra.setDuracaoReserva(updateDTO.getDuracaoReserva());
        }

        if (updateDTO.getCobertura() != null) {
            quadra.setCobertura(updateDTO.getCobertura());
        }

        if (updateDTO.getIluminacaoNoturna() != null) {
            quadra.setIluminacaoNoturna(updateDTO.getIluminacaoNoturna());
        }

        if (updateDTO.getMateriaisFornecidos() != null) {
            quadra.setMateriaisFornecidos(updateDTO.getMateriaisFornecidos());
        }
    }

    private void atualizarHorariosFuncionamento(Quadra quadra, List<HorarioFuncionamentoUpdateDTO> horariosDTO) {
        for (HorarioFuncionamentoUpdateDTO horarioDTO : horariosDTO) {
            HorarioFuncionamento horarioExistente = quadra.getHorariosFuncionamento().stream()
                    .filter(h -> h.getDiaDaSemana().equals(horarioDTO.getDiaDaSemana()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Horário de funcionamento não encontrado para o dia: " + horarioDTO.getDiaDaSemana()));

            if (horarioDTO.getIntervalosDeHorario() != null) {
                atualizarIntervalosDeHorario(horarioExistente, horarioDTO.getIntervalosDeHorario(), quadra);
            }
        }
    }

    private void atualizarIntervalosDeHorario(HorarioFuncionamento horarioFuncionamento,
                                              List<IntervaloHorarioUpdateDTO> intervalosDTO,
                                              Quadra quadra) {

        // Validar intervalos antes de processar
        validarIntervalosParaAtualizacao(intervalosDTO);

        // Processar intervalos existentes (com ID)
        for (IntervaloHorarioUpdateDTO intervaloDTO : intervalosDTO) {
            if (intervaloDTO.getId() != null) {
                atualizarIntervaloExistente(horarioFuncionamento, intervaloDTO, quadra);
            }
        }

        // Processar novos intervalos (sem ID)
        for (IntervaloHorarioUpdateDTO intervaloDTO : intervalosDTO) {
            if (intervaloDTO.getId() == null) {
                criarNovoIntervalo(horarioFuncionamento, intervaloDTO, quadra);
            }
        }
    }

    private void validarIntervalosParaAtualizacao(List<IntervaloHorarioUpdateDTO> intervalosDTO) {
        // Converter DTOs para objetos temporários para validação
        List<IntervaloHorario> intervalosTemp = intervalosDTO.stream()
                .map(dto -> IntervaloHorario.builder()
                        .inicio(dto.getInicio())
                        .fim(dto.getFim())
                        .build())
                .collect(Collectors.toList());

        validarIntervalosDeHorario(intervalosTemp);
    }

    private void atualizarIntervaloExistente(HorarioFuncionamento horarioFuncionamento,
                                             IntervaloHorarioUpdateDTO intervaloDTO,
                                             Quadra quadra) {

        IntervaloHorario intervaloExistente = horarioFuncionamento.getIntervalosDeHorario().stream()
                .filter(i -> i.getId().equals(intervaloDTO.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Intervalo de horário não encontrado com ID: " + intervaloDTO.getId()));

        // Verificar se há agendamentos pendentes nos slots deste intervalo
        verificarAgendamentosPendentes(intervaloExistente);

        // Verificar se houve mudança nos horários
        boolean houveMudancaHorarios = !intervaloExistente.getInicio().equals(intervaloDTO.getInicio()) ||
                !intervaloExistente.getFim().equals(intervaloDTO.getFim());

        // Atualizar os dados do intervalo
        intervaloExistente.setInicio(intervaloDTO.getInicio());
        intervaloExistente.setFim(intervaloDTO.getFim());
        intervaloExistente.setValor(intervaloDTO.getValor());
        intervaloExistente.setStatus(intervaloDTO.getStatus());

        // Se houve mudança nos horários, recriar os slots
        if (houveMudancaHorarios) {
            recriarSlotsComRepositorio(intervaloExistente, quadra);
        } else {
            // Se não houve mudança nos horários, apenas atualizar valor e status dos slots
            atualizarSlotsExistentes(intervaloExistente);
        }
    }

    private void verificarAgendamentosPendentes(IntervaloHorario intervalo) {
        boolean temAgendamentosPendentes = intervalo.getSlotsHorario().stream()
                .anyMatch(slot -> slot.getAgendamentos().stream()
                        .anyMatch(agendamento ->
                                agendamento.getDataAgendamento().isAfter(LocalDate.now()) ||
                                        (agendamento.getDataAgendamento().isEqual(LocalDate.now()) &&
                                                slot.getHorarioInicio().isAfter(LocalTime.now()))));

        if (temAgendamentosPendentes) {
            throw new IntervaloComAgendamentosException(
                    "Não é possível atualizar o intervalo de horário pois existem agendamentos pendentes.");
        }
    }

    private void atualizarSlotsExistentes(IntervaloHorario intervalo) {
        StatusDisponibilidade statusSlot = mapearStatusDisponibilidade(intervalo.getStatus());

        for (SlotHorario slot : intervalo.getSlotsHorario()) {
            slot.setValor(intervalo.getValor());
            slot.setStatusDisponibilidade(statusSlot);
        }
    }

    private void criarNovoIntervalo(HorarioFuncionamento horarioFuncionamento,
                                    IntervaloHorarioUpdateDTO intervaloDTO,
                                    Quadra quadra) {

        IntervaloHorario novoIntervalo = IntervaloHorario.builder()
                .inicio(intervaloDTO.getInicio())
                .fim(intervaloDTO.getFim())
                .valor(intervaloDTO.getValor())
                .status(intervaloDTO.getStatus())
                .horarioFuncionamento(horarioFuncionamento)
                .build();

        // Gerar slots para o novo intervalo
        List<SlotHorario> slots = slotHorarioService.gerarSlotsParaIntervalo(
                novoIntervalo, quadra.getDuracaoReserva());
        novoIntervalo.setSlotsHorario(slots);

        horarioFuncionamento.getIntervalosDeHorario().add(novoIntervalo);
    }

    private StatusDisponibilidade mapearStatusDisponibilidade(StatusIntervalo statusIntervalo) {
        return switch (statusIntervalo) {
            case DISPONIVEL -> StatusDisponibilidade.DISPONIVEL;
            case INDISPONIVEL -> StatusDisponibilidade.INDISPONIVEL;
            case MANUTENCAO -> StatusDisponibilidade.MANUTENCAO;
        };
    }

    @Override
    public Quadra buscarPorId(Long id) {
        return quadraRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + id));
    }

    @Override
    public Page<Quadra> listarTodos(Pageable pageable, Long arenaId, String esporte) {
        Specification<Quadra> spec = (root, query, builder) -> null;

        if (arenaId != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("arena").get("id"), arenaId));
        }

        if (esporte != null && !esporte.trim().isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.isMember(esporte, root.get("tipoQuadra")));
        }

        return quadraRepository.findAll(spec, pageable);
    }

    private void validarDadosUnicos(String nome) {
        if (quadraRepository.existsByNomeQuadraIgnoreCase(nome)) {
            throw new UniqueConstraintViolationException("Nome de quadra já está em uso.");
        }
    }

    private void validarHorariosFuncionamento(List<HorarioFuncionamento> horarios) {
        Set<DiaDaSemana> dias = new HashSet<>();
        for (HorarioFuncionamento horario : horarios) {
            if (!dias.add(horario.getDiaDaSemana())) {
                throw new DuplicateHorarioFuncionamentoException(
                        "Horário de funcionamento duplicado para o dia: " + horario.getDiaDaSemana());
            }
            if (!horario.getIntervalosDeHorario().isEmpty()) {
                validarIntervalosDeHorario(horario.getIntervalosDeHorario());
            }
        }
    }

    private void validarIntervalosDeHorario(List<IntervaloHorario> intervalos) {
        for (IntervaloHorario intervalo : intervalos) {
            if (intervalo.getInicio() == null || intervalo.getFim() == null) {
                throw new InvalidIntervaloHorarioException("Horário de início e fim devem ser informados.");
            }
            if (!intervalo.getInicio().isBefore(intervalo.getFim())) {
                throw new InvalidIntervaloHorarioException(
                        "Horário de início (" + intervalo.getInicio() + ") deve ser anterior ao horário de fim ("
                                + intervalo.getFim() + ").");
            }
            if (intervalo.getInicio().isBefore(LocalTime.of(0, 0)) || intervalo.getFim().isAfter(
                    LocalTime.of(23, 59))) {
                throw new InvalidIntervaloHorarioException("Os horários devem estar entre 00:00 e 23:59.");
            }
        }

        for (int i = 0; i < intervalos.size(); i++) {
            IntervaloHorario intervalo1 = intervalos.get(i);
            for (int j = i + 1; j < intervalos.size(); j++) {
                IntervaloHorario intervalo2 = intervalos.get(j);
                if (isSobrepostoOuRepetido(intervalo1, intervalo2)) {
                    throw new InvalidIntervaloHorarioException(
                            "Intervalos de horário sobrepostos ou repetidos: [" + intervalo1.getInicio() + "-"
                                    + intervalo1.getFim() + "] e ["
                                    + intervalo2.getInicio() + "-"
                                    + intervalo2.getFim() + "]");
                }
            }
        }
    }

    private boolean isSobrepostoOuRepetido(IntervaloHorario intervalo1, IntervaloHorario intervalo2) {
        return !(intervalo1.getFim().compareTo(intervalo2.getInicio()) <= 0 ||
                intervalo2.getFim().compareTo(intervalo1.getInicio()) <= 0);
    }

    @Override
    @Transactional
    public void excluir(Long id, Long arenaId) {
        Quadra quadra = quadraRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + id));

        if (!quadra.getArena().getId().equals(arenaId)) {
            throw new AccessDeniedException("Usuário não autorizado para excluir esta quadra");
        }

        quadraRepository.delete(quadra);
    }

    @Override
    public List<Quadra> buscarPorArenaId(Long arenaId) {
        List<Quadra> quadras = quadraRepository.findByArenaId(arenaId);
        if (quadras.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma quadra encontrada para a arena com ID: " + arenaId);
        }
        return quadras;
    }

    /**
     * Consulta disponibilidade de slots para uma data específica
     */
    public List<SlotHorarioResponseDTO> consultarDisponibilidade(Long quadraId, LocalDate data) {
        Quadra quadra = quadraRepository.findById(quadraId)
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada com ID: " + quadraId));

        // Determinar o dia da semana
        DiaDaSemana diaDaSemana = DiaDaSemana.fromLocalDate(data);

        // Buscar horário de funcionamento específico para o dia
        Optional<HorarioFuncionamento> horarioFuncionamento = quadra.getHorariosFuncionamento()
                .stream()
                .filter(h -> h.getDiaDaSemana() == diaDaSemana)
                .findFirst();

        // Se não há horário de funcionamento para o dia, retorna lista vazia
        if (horarioFuncionamento.isEmpty()) {
            return new ArrayList<>();
        }

        // Buscar todos os slots do dia específico
        List<SlotHorario> slotHorarios = horarioFuncionamento.get()
                .getIntervalosDeHorario()
                .stream()
                .flatMap(intervalo -> intervalo.getSlotsHorario().stream())
                .toList();

        // Se não há slots para o dia, retorna lista vazia
        if (slotHorarios.isEmpty()) {
            return new ArrayList<>();
        }

        return verificarDisponibilidadeSlotsParaData(slotHorarios, data, quadraId)
                .stream()
                .map(quadraMapper::mapearSlotParaResponse)
                .toList();
    }

    private List<SlotHorario> verificarDisponibilidadeSlotsParaData(
            List<SlotHorario> slots,
            LocalDate dataAgendamento,
            Long quadraId) {

        List<SlotHorario> slotsDisponiveis = new ArrayList<>();

        for (SlotHorario slot : slots) {
            // 1. Verificar se o slot está fisicamente disponível
            if (slot.getStatusDisponibilidade() == StatusDisponibilidade.MANUTENCAO ||
                    slot.getStatusDisponibilidade() == StatusDisponibilidade.INDISPONIVEL) {
                continue; // pula para o próximo slot
            }

            // 2. Verificar se já existe agendamento para este slot na data específica
            boolean jaAgendado = agendamentoRepository.existeConflito(
                    dataAgendamento,
                    quadraId,
                    slot.getHorarioInicio(),
                    slot.getHorarioFim()
            );

            if (!jaAgendado) {
                slotsDisponiveis.add(slot);
            }
        }

        return slotsDisponiveis;
    }

    // Solução alternativa usando repositório diretamente
    private void recriarSlotsComRepositorio(IntervaloHorario intervalo, Quadra quadra) {
        // Verificar se todos os slots podem ser removidos
        for (SlotHorario slot : intervalo.getSlotsHorario()) {
            if (!slot.getAgendamentos().isEmpty()) {
                throw new IntervaloComAgendamentosException(
                        "Não é possível recriar slots pois existem agendamentos no slot: " +
                                slot.getHorarioInicio() + " - " + slot.getHorarioFim());
            }
        }

        // Coletar IDs dos slots para remoção
        List<Long> slotIds = intervalo.getSlotsHorario().stream()
                .map(SlotHorario::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Limpar a coleção primeiro
        intervalo.getSlotsHorario().clear();

        // Flush para garantir que as mudanças sejam persistidas
        entityManager.flush();

        // Remover slots pelo repositório se necessário
        if (!slotIds.isEmpty()) {
            slotHorarioRepository.deleteAllById(slotIds);
            entityManager.flush();
        }

        // Gerar novos slots
        List<SlotHorario> novosSlots = slotHorarioService.gerarSlotsParaIntervalo(
                intervalo, quadra.getDuracaoReserva());

        // Adicionar novos slots
        intervalo.getSlotsHorario().addAll(novosSlots);
    }
}