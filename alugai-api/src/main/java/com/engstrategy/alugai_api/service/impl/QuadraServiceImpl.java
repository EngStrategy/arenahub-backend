package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.quadra.QuadraUpdateDTO;
import com.engstrategy.alugai_api.exceptions.DuplicateHorarioFuncionamentoException;
import com.engstrategy.alugai_api.exceptions.InvalidIntervaloHorarioException;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Arena;
import com.engstrategy.alugai_api.model.HorarioFuncionamento;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.repository.ArenaRepository;
import com.engstrategy.alugai_api.repository.QuadraRepository;
import com.engstrategy.alugai_api.service.QuadraService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuadraServiceImpl implements QuadraService {

    private final QuadraRepository quadraRepository;
    private final ArenaRepository arenaRepository;

    @Override
    @Transactional
    public Quadra criarQuadra(Quadra quadra, Long arenaId) {
        validarDadosUnicos(quadra.getNomeQuadra());
        validarHorariosFuncionamento(quadra.getHorariosFuncionamento());

        Arena arena = arenaRepository.findById(arenaId)
                .orElseThrow(() -> new UserNotFoundException("Arena não encontrada com ID: " + arenaId));

        quadra.getHorariosFuncionamento().forEach(horario -> horario.setQuadra(quadra));
        quadra.setArena(arena);
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

    @Override
    @Transactional
    public Quadra atualizar(Long id, QuadraUpdateDTO quadraUpdateDTO) {
        Quadra savedQuadra = quadraRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + id));

        if (quadraUpdateDTO.getNomeQuadra() != null) {
            savedQuadra.setNomeQuadra(quadraUpdateDTO.getNomeQuadra());
        }
        if (quadraUpdateDTO.getUrlFotoQuadra() != null) {
            savedQuadra.setUrlFotoQuadra(quadraUpdateDTO.getUrlFotoQuadra());
        }
        if (quadraUpdateDTO.getTipoQuadra() != null) {
            savedQuadra.setTipoQuadra(quadraUpdateDTO.getTipoQuadra());
        }
        if (quadraUpdateDTO.getDescricao() != null) {
            savedQuadra.setDescricao(quadraUpdateDTO.getDescricao());
        }
        if (quadraUpdateDTO.getDuracaoReserva() != null) {
            savedQuadra.setDuracaoReserva(quadraUpdateDTO.getDuracaoReserva());
        }
        if (quadraUpdateDTO.getCobertura() != null) {
            savedQuadra.setCobertura(quadraUpdateDTO.getCobertura());
        }
        if (quadraUpdateDTO.getIluminacaoNoturna() != null) {
            savedQuadra.setIluminacaoNoturna(quadraUpdateDTO.getIluminacaoNoturna());
        }
        if (quadraUpdateDTO.getMateriaisFornecidos() != null) {
            savedQuadra.setMateriaisFornecidos(quadraUpdateDTO.getMateriaisFornecidos());
        }

        return quadraRepository.save(savedQuadra);
    }

    private void validarDadosUnicos(String nome) {
        if (quadraRepository.existsByNomeQuadra(nome)) {
            throw new UniqueConstraintViolationException("Nome já está em uso.");
        }
    }

    private void validarHorariosFuncionamento(List<HorarioFuncionamento> horarios) {
        // Verificar unicidade de dia da semana
        Set<DiaDaSemana> dias = new HashSet<>();
        for (HorarioFuncionamento horario : horarios) {
            if (!dias.add(horario.getDiaDaSemana())) {
                throw new DuplicateHorarioFuncionamentoException(
                        "Horário de funcionamento duplicado para o dia: " + horario.getDiaDaSemana());
            }
            validarIntervalosDeHorario(horario.getIntervalosDeHorario());
        }
    }

    private void validarIntervalosDeHorario(List<IntervaloHorario> intervalos) {
        for (IntervaloHorario intervalo : intervalos) {
            // Verificar se início é antes de fim
            if (intervalo.getInicio() == null || intervalo.getFim() == null) {
                throw new InvalidIntervaloHorarioException("Horário de início e fim devem ser informados.");
            }

            // Verificar se início vem antes do fim
            if (!intervalo.getInicio().isBefore(intervalo.getFim())) {
                throw new InvalidIntervaloHorarioException(
                        "Horário de início (" + intervalo.getInicio() + ") deve ser anterior ao horário de fim (" + intervalo.getFim() + ").");
            }

            // Verificar se o intervalo está dentro de limites realistas
            if (intervalo.getInicio().isBefore(LocalTime.of(0, 0)) || intervalo.getFim().isAfter(LocalTime.of(23, 59))) {
                throw new InvalidIntervaloHorarioException("Os horários devem estar entre 00:00 e 23:59.");
            }
        }

        // Verificar sobreposições ou intervalos repetidos
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
        // Intervalos são sobrepostos se um começa antes ou no mesmo momento que o outro termina
        return !(intervalo1.getFim().compareTo(intervalo2.getInicio()) <= 0 || intervalo2.getFim().compareTo(intervalo1.getInicio()) <= 0);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Quadra quadra = quadraRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Quadra não encontrada com ID: " + id));
        quadraRepository.delete(quadra);
    }
}