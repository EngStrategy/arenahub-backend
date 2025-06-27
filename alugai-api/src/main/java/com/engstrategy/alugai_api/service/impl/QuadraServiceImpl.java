package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.quadra.HorarioFuncionamentoUpdateDTO;
import com.engstrategy.alugai_api.dto.quadra.IntervaloHorarioUpdateDTO;
import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.exceptions.*;
import com.engstrategy.alugai_api.mapper.QuadraMapper;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.QuadraRepository;
import com.engstrategy.alugai_api.service.QuadraService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuadraServiceImpl implements QuadraService {

    private final QuadraRepository quadraRepository;
    private final ArenaRepository arenaRepository;
    private final QuadraMapper quadraMapper;

    @Override
    @Transactional
    public Quadra criarQuadra(Quadra quadra, Long arenaId) {
        // As implemented previously
        validarDadosUnicos(quadra.getNomeQuadra());
        validarHorariosFuncionamento(quadra.getHorariosFuncionamento());

        if (quadra.getHorariosFuncionamento().size() != 7) {
            throw new IllegalArgumentException("A quadra deve ter exatamente 7 horários de funcionamento, um para cada dia da semana.");
        }

        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + arenaId));

        quadra.getHorariosFuncionamento().forEach(horario -> horario.setQuadra(quadra));
        quadra.setArena(arena);
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
        quadraMapper.updateQuadraFromDto(updateDTO, quadra);

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
        if (quadraRepository.existsByNomeQuadra(nome)) {
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
                        "Horário de início (" + intervalo.getInicio() + ") deve ser anterior ao horário de fim (" + intervalo.getFim() + ").");
            }
            if (intervalo.getInicio().isBefore(LocalTime.of(0, 0)) || intervalo.getFim().isAfter(LocalTime.of(23, 59))) {
                throw new InvalidIntervaloHorarioException("Os horários devem estar entre 00:00 e 23:59.");
            }
        }

        for (int i = 0; i < intervalos.size(); i++) {
            IntervaloHorario intervalo1 = intervalos.get(i);
            for (int j = i + 1; j < intervalos.size(); j++) {
                IntervaloHorario intervalo2 = intervalos.get(j);
                if (isSobrepostoOuRepetido(intervalo1, intervalo2)) {
                    throw new InvalidIntervaloHorarioException(
                            "Intervalos de horário sobrepostos ou repetidos: [" + intervalo1.getInicio() + "-" + intervalo1.getFim() + "] e [" +
                                    intervalo2.getInicio() + "-" + intervalo2.getFim() + "]");
                }
            }
        }
    }

    private boolean isSobrepostoOuRepetido(IntervaloHorario intervalo1, IntervaloHorario intervalo2) {
        return !(intervalo1.getFim().compareTo(intervalo2.getInicio()) <= 0 || intervalo2.getFim().compareTo(intervalo1.getInicio()) <= 0);
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
}