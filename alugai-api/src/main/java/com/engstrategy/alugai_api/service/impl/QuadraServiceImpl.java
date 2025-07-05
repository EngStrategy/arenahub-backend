package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.quadra.*;
import com.engstrategy.alugai_api.exceptions.*;
import com.engstrategy.alugai_api.mapper.QuadraMapper;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.StatusDisponibilidade;
import com.engstrategy.alugai_api.repository.*;
import com.engstrategy.alugai_api.service.AgendamentoService;
import com.engstrategy.alugai_api.service.QuadraService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuadraServiceImpl implements QuadraService {

    private final QuadraRepository quadraRepository;
    private final ArenaRepository arenaRepository;
    private final HorarioFuncionamentoRepository horarioFuncionamentoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final SlotHorarioService slotHorarioService;
    private final QuadraMapper quadraMapper;

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

        // Validate unique constraints for nomeQuadra if updated
        if (updateDTO.getNomeQuadra() != null && !updateDTO.getNomeQuadra().equals(quadra.getNomeQuadra())) {
            validarDadosUnicos(updateDTO.getNomeQuadra());
        }

        // Validate HorarioFuncionamento if updated
        if (updateDTO.getHorariosFuncionamento() != null && !updateDTO.getHorariosFuncionamento().isEmpty()) {
            // Map existing HorarioFuncionamento by day for validation
            Map<DiaDaSemana, HorarioFuncionamento> existingHorarios = quadra.getHorariosFuncionamento()
                    .stream()
                    .collect(Collectors.toMap(HorarioFuncionamento::getDiaDaSemana, Function.identity()));

            // Check for duplicate days and validate intervals
            Set<DiaDaSemana> dias = new HashSet<>();
            for (HorarioFuncionamentoUpdateDTO horarioDTO : updateDTO.getHorariosFuncionamento()) {
                DiaDaSemana dia = horarioDTO.getDiaDaSemana();
                if (!dias.add(dia)) {
                    throw new DuplicateHorarioFuncionamentoException(
                            "Horário de funcionamento duplicado para o dia: " + dia);
                }
                if (!existingHorarios.containsKey(dia)) {
                    throw new IllegalArgumentException("Horário de funcionamento não encontrado para o dia: " + dia);
                }

                // Validate intervals if provided
                if (horarioDTO.getIntervalosDeHorario() != null && !horarioDTO.getIntervalosDeHorario().isEmpty()) {
                    // Map existing intervals for ID validation
                    Map<Long, IntervaloHorario> existingIntervals = existingHorarios.get(dia).getIntervalosDeHorario()
                            .stream()
                            .collect(Collectors.toMap(IntervaloHorario::getId, Function.identity()));

                    // Validate interval IDs and collect intervals for validation
                    List<IntervaloHorario> tempIntervals = new ArrayList<>();
                    for (IntervaloHorarioUpdateDTO intervalDTO : horarioDTO.getIntervalosDeHorario()) {
                        if (intervalDTO.getId() != null && !existingIntervals.containsKey(intervalDTO.getId())) {
                            throw new IllegalArgumentException("Intervalo de horário com ID " + intervalDTO.getId() + " não encontrado para o dia: " + dia);
                        }
                        tempIntervals.add(IntervaloHorario.builder()
                                .inicio(intervalDTO.getInicio())
                                .fim(intervalDTO.getFim())
                                .valor(intervalDTO.getValor())
                                .status(intervalDTO.getStatus())
                                .build());
                    }
                    validarIntervalosDeHorario(tempIntervals);
                }
            }
        }

        // Apply updates
        updateQuadraFromDto(updateDTO, quadra);

        return quadraRepository.save(quadra);
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
            throw new UniqueConstraintViolationException("Nome já está em uso.");
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

    private void updateQuadraFromDto(QuadraUpdateDTO updateDTO, Quadra quadra) {
        // Update simple attributes if provided
        if (updateDTO.getNomeQuadra() != null) {
            quadra.setNomeQuadra(updateDTO.getNomeQuadra());
        }
        if (updateDTO.getUrlFotoQuadra() != null) {
            quadra.setUrlFotoQuadra(updateDTO.getUrlFotoQuadra());
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

        // Update HorarioFuncionamento if provided
        if (updateDTO.getHorariosFuncionamento() != null && !updateDTO.getHorariosFuncionamento().isEmpty()) {
            // Map existing HorarioFuncionamento by day
            Map<DiaDaSemana, HorarioFuncionamento> existingHorarios = quadra.getHorariosFuncionamento()
                    .stream()
                    .collect(Collectors.toMap(HorarioFuncionamento::getDiaDaSemana, Function.identity()));

            // Process updated HorarioFuncionamento
            for (HorarioFuncionamentoUpdateDTO updateHorarioDTO : updateDTO.getHorariosFuncionamento()) {
                DiaDaSemana dia = updateHorarioDTO.getDiaDaSemana();
                HorarioFuncionamento horario = existingHorarios.get(dia);
                if (horario == null) {
                    throw new IllegalArgumentException("Horário de funcionamento não encontrado para o dia: " + dia);
                }

                // Map existing IntervaloHorario by ID
                Map<Long, IntervaloHorario> existingIntervals = horario.getIntervalosDeHorario()
                        .stream()
                        .collect(Collectors.toMap(IntervaloHorario::getId, Function.identity()));

                // Clear existing intervals to ensure removals are detected
                horario.getIntervalosDeHorario().clear();

                // Process provided intervals
                if (updateHorarioDTO.getIntervalosDeHorario() != null && !updateHorarioDTO.getIntervalosDeHorario().isEmpty()) {
                    List<IntervaloHorario> updatedIntervals = new ArrayList<>();
                    for (IntervaloHorarioUpdateDTO intervalDTO : updateHorarioDTO.getIntervalosDeHorario()) {
                        IntervaloHorario intervalo;
                        if (intervalDTO.getId() != null && existingIntervals.containsKey(intervalDTO.getId())) {
                            // Update existing interval
                            intervalo = existingIntervals.get(intervalDTO.getId());
                            if (intervalDTO.getInicio() != null) {
                                intervalo.setInicio(intervalDTO.getInicio());
                            }
                            if (intervalDTO.getFim() != null) {
                                intervalo.setFim(intervalDTO.getFim());
                            }
                            if (intervalDTO.getValor() != null) {
                                intervalo.setValor(intervalDTO.getValor());
                            }
                            if (intervalDTO.getStatus() != null) {
                                intervalo.setStatus(intervalDTO.getStatus());
                            }
                        } else {
                            // Create new interval
                            intervalo = IntervaloHorario.builder()
                                    .inicio(intervalDTO.getInicio())
                                    .fim(intervalDTO.getFim())
                                    .valor(intervalDTO.getValor())
                                    .status(intervalDTO.getStatus())
                                    .horarioFuncionamento(horario)
                                    .build();
                        }
                        updatedIntervals.add(intervalo);
                    }
                    // Add updated intervals
                    horario.getIntervalosDeHorario().addAll(updatedIntervals);
                }

                // Save HorarioFuncionamento to ensure changes are persisted
                horarioFuncionamentoRepository.save(horario);
            }
        }
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

        List<SlotHorario> slotHorarios = quadra.getAllSlotHorarios();

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
}