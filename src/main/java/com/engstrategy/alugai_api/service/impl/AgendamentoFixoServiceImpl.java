package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.exceptions.AccessDeniedException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.PeriodoAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamentoFixo;
import com.engstrategy.alugai_api.repository.AgendamentoFixoRepository;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.repository.SlotHorarioRepository;
import com.engstrategy.alugai_api.service.AgendamentoFixoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AgendamentoFixoServiceImpl implements AgendamentoFixoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoFixoRepository agendamentoFixoRepository;
    private final SlotHorarioService slotHorarioService;
    private final SlotHorarioRepository slotHorarioRepository;
    private final AtletaRepository atletaRepository;

    @Override
    public AgendamentoFixo criarAgendamentosFixos(Agendamento agendamentoBase) {
        log.info("Iniciando criação de agendamentos fixos para o agendamento base ID: {}",
                agendamentoBase.getId());

        // Criar entidade AgendamentoFixo
        AgendamentoFixo agendamentoFixo = criarEntidadeAgendamentoFixo(agendamentoBase);
        agendamentoFixo = agendamentoFixoRepository.save(agendamentoFixo);

        // Associar agendamento base ao agendamento fixo
        agendamentoBase.setAgendamentoFixo(agendamentoFixo);
        agendamentoRepository.save(agendamentoBase);

        // Gerar agendamentos futuros
        List<Agendamento> agendamentosFuturos = gerarAgendamentosFuturos(agendamentoBase, agendamentoFixo);

        // Salvar agendamentos futuros
        agendamentoRepository.saveAll(agendamentosFuturos);

        log.info("Agendamentos fixos criados com sucesso. Total: {} agendamentos",
                agendamentosFuturos.size() + 1);

        return agendamentoFixo;
    }

    private AgendamentoFixo criarEntidadeAgendamentoFixo(Agendamento agendamentoBase) {
        LocalDate dataInicio = agendamentoBase.getDataAgendamento();
        LocalDate dataFim = calcularDataFim(dataInicio, agendamentoBase.getPeriodoAgendamentoFixo());

        return AgendamentoFixo.builder()
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .periodo(agendamentoBase.getPeriodoAgendamentoFixo())
                .atleta(agendamentoBase.getAtleta())
                .build();
    }

    private List<Agendamento> gerarAgendamentosFuturos(Agendamento agendamentoBase,
                                                       AgendamentoFixo agendamentoFixo) {
        List<Agendamento> agendamentosFuturos = new ArrayList<>();
        List<LocalDate> datasConflito = new ArrayList<>();

        LocalDate dataInicio = agendamentoBase.getDataAgendamento();
        LocalDate dataFim = agendamentoFixo.getDataFim();
        LocalDate dataAtual = dataInicio.plusWeeks(1); // Próxima semana

        while (!dataAtual.isAfter(dataFim)) {
            try {
                // Verificar disponibilidade dos slots para a data
                if (verificarDisponibilidadeParaData(agendamentoBase, dataAtual)) {
                    Agendamento novoAgendamento = criarAgendamentoFuturo(agendamentoBase, dataAtual, agendamentoFixo);
                    agendamentosFuturos.add(novoAgendamento);
                } else {
                    datasConflito.add(dataAtual);
                    log.warn("Conflito encontrado na data: {} - slots não disponíveis", dataAtual);
                }
            } catch (Exception e) {
                log.error("Erro ao processar data {}: {}", dataAtual, e.getMessage());
                datasConflito.add(dataAtual);
            }

            dataAtual = dataAtual.plusWeeks(1);
        }

        if (!datasConflito.isEmpty()) {
            log.warn("Foram encontrados conflitos nas seguintes datas: {}", datasConflito);
        }

        return agendamentosFuturos;
    }

    private boolean verificarDisponibilidadeParaData(Agendamento agendamentoBase, LocalDate data) {
        // Verificar se os slots correspondentes existem e estão disponíveis
        List<SlotHorario> slotsNecessarios = buscarSlotsCorrespondentesParaData(
                agendamentoBase.getSlotsHorario(), data);

        if (slotsNecessarios.size() != agendamentoBase.getSlotsHorario().size()) {
            return false; // Nem todos os slots existem para esta data
        }

        // Verificar se há conflitos de agendamento
        for (SlotHorario slot : slotsNecessarios) {
            boolean temConflito = agendamentoRepository.existeConflito(
                    data,
                    agendamentoBase.getQuadra().getId(),
                    slot.getHorarioInicio(),
                    slot.getHorarioFim()
            );

            if (temConflito) {
                return false;
            }
        }

        return true;
    }

    private List<SlotHorario> buscarSlotsCorrespondentesParaData(List<SlotHorario> slotsOriginais,
                                                                 LocalDate data) {
        DiaDaSemana diaSemana = DiaDaSemana.values()[data.getDayOfWeek().getValue() - 1];
        List<SlotHorario> slotsCorrespondentes = new ArrayList<>();

        for (SlotHorario slotOriginal : slotsOriginais) {
            Optional<SlotHorario> slotCorrespondente = slotHorarioRepository
                    .findByIntervaloHorario_HorarioFuncionamento_DiaDaSemanaAndHorarioInicioAndHorarioFim(
                            diaSemana,
                            slotOriginal.getHorarioInicio(),
                            slotOriginal.getHorarioFim()
                    );

            if (slotCorrespondente.isPresent()) {
                slotsCorrespondentes.add(slotCorrespondente.get());
            } else {
                // Se não encontrar o slot correspondente, retorna lista vazia
                return new ArrayList<>();
            }
        }

        return slotsCorrespondentes;
    }

    private Agendamento criarAgendamentoFuturo(Agendamento agendamentoBase,
                                               LocalDate novaData,
                                               AgendamentoFixo agendamentoFixo) {
        List<SlotHorario> slotsCorrespondentes = buscarSlotsCorrespondentesParaData(
                agendamentoBase.getSlotsHorario(), novaData);

        return Agendamento.builder()
                .dataAgendamento(novaData)
                .esporte(agendamentoBase.getEsporte())
                .isFixo(true)
                .isPublico(false) // Agendamentos fixos não podem ser públicos
                .periodoAgendamentoFixo(agendamentoBase.getPeriodoAgendamentoFixo())
                .vagasDisponiveis(agendamentoBase.getVagasDisponiveis())
                .status(StatusAgendamento.PENDENTE)
                .quadra(agendamentoBase.getQuadra())
                .atleta(agendamentoBase.getAtleta())
                .agendamentoFixo(agendamentoFixo)
                .slotsHorario(slotsCorrespondentes)
                .build();
    }

    private LocalDate calcularDataFim(LocalDate dataInicio, PeriodoAgendamento periodo) {
        return switch (periodo) {
            case UM_MES -> dataInicio.plusMonths(1);
            case TRES_MESES -> dataInicio.plusMonths(3);
            case SEIS_MESES -> dataInicio.plusMonths(6);
            default -> throw new IllegalArgumentException("Período não reconhecido: " + periodo);
        };
    }

    @Override
    public void cancelarAgendamentoFixo(Long agendamentoFixoId, UUID usuarioId) {
        log.info("Cancelando agendamento fixo ID: {}", agendamentoFixoId);

        AgendamentoFixo agendamentoFixo = agendamentoFixoRepository.findById(agendamentoFixoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento fixo não encontrado com id: " + agendamentoFixoId));

        Atleta atleta = atletaRepository.findById(usuarioId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: + " + usuarioId));

        if (!agendamentoFixo.getAtleta().getId().equals(atleta.getId())) {
            throw new AccessDeniedException("Usuário não autorizado para cancelar este agendamento!");
        }

        // Verificar se já não está cancelado
        if (agendamentoFixo.getStatus() == StatusAgendamentoFixo.CANCELADO) {
            throw new IllegalArgumentException("Agendamento já está cancelado");
        }

        // Buscar todos os agendamentos futuros
        List<Agendamento> agendamentosFuturos = agendamentoRepository
                .findByAgendamentoFixoId(agendamentoFixoId);

        LocalDate hoje = LocalDate.now();

        // Cancelar agendamentos futuros e liberar slots
        for (Agendamento agendamento : agendamentosFuturos) {
            if (agendamento.getDataAgendamento().isAfter(hoje)) {
                agendamento.setStatus(StatusAgendamento.CANCELADO);
            }
        }

        // Marcar agendamento fixo como cancelado
        agendamentoFixo.setStatus(StatusAgendamentoFixo.CANCELADO);
        agendamentoFixoRepository.save(agendamentoFixo);

        log.info("Agendamento fixo cancelado com sucesso");
    }

    @Override
    public List<AgendamentoFixo> listarAgendamentosFixosAtivos(UUID atletaId) {
        return agendamentoFixoRepository.findByAtletaIdAndStatus(atletaId, StatusAgendamentoFixo.ATIVO);
    }

    @Override
    public AgendamentoFixo buscarPorId(Long id) {
        return agendamentoFixoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento fixo não encontrado"));
    }
}
