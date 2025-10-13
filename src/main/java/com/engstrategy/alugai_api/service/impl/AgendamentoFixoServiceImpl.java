package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.exceptions.AccessDeniedException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.AgendamentoFixo;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.SlotHorario;
import com.engstrategy.alugai_api.model.enums.*;
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
import java.util.*;

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
        log.info("Iniciando criação de agendamentos fixos para o agendamento base ID: {}", agendamentoBase.getId());

        // Calcular o limite teórico para a geração dos agendamentos.
        LocalDate dataLimite = calcularDataFim(agendamentoBase.getDataAgendamento(), agendamentoBase.getPeriodoAgendamentoFixo());

        // Criar a entidade AgendamentoFixo EM MEMÓRIA, sem salvar ainda.
        AgendamentoFixo agendamentoFixo = AgendamentoFixo.builder()
                .dataInicio(agendamentoBase.getDataAgendamento())
                .periodo(agendamentoBase.getPeriodoAgendamentoFixo())
                .atleta(agendamentoBase.getAtleta())
                .build();

        // Gerar os agendamentos futuros passando o limite teórico.
        List<Agendamento> agendamentosFuturos = gerarAgendamentosFuturos(agendamentoBase, dataLimite, agendamentoFixo);

        // Determinar a data final REAL baseada no último agendamento criado com sucesso.
        LocalDate dataFimReal;
        if (!agendamentosFuturos.isEmpty()) {
            // A data final é a data do último agendamento na lista gerada.
            dataFimReal = agendamentosFuturos.get(agendamentosFuturos.size() - 1).getDataAgendamento();
        } else {
            // Se nenhum agendamento futuro pôde ser criado (todos deram conflito),
            // a data final é a mesma da data de início.
            dataFimReal = agendamentoBase.getDataAgendamento();
        }
        agendamentoFixo.setDataFim(dataFimReal);

        // Salvar a entidade AgendamentoFixo com a data final correta.
        agendamentoFixo = agendamentoFixoRepository.save(agendamentoFixo);

        // Associar o pai (agendamentoFixo) a todos os filhos (base e futuros).
        agendamentoBase.setAgendamentoFixo(agendamentoFixo);
        for (Agendamento futuro : agendamentosFuturos) {
            futuro.setAgendamentoFixo(agendamentoFixo);
        }

        // Salvar as alterações no agendamento base e os novos agendamentos futuros.
        agendamentoRepository.save(agendamentoBase);
        agendamentoRepository.saveAll(agendamentosFuturos);

        log.info("Agendamentos fixos criados com sucesso. Data de início: {}, Data de fim real: {}. Total: {} agendamentos.",
                agendamentoFixo.getDataInicio(), agendamentoFixo.getDataFim(), agendamentosFuturos.size() + 1);

        return agendamentoFixo;
    }

    private List<Agendamento> gerarAgendamentosFuturos(Agendamento agendamentoBase,
                                                       LocalDate dataLimite,
                                                       AgendamentoFixo agendamentoFixoPai) {
        List<Agendamento> agendamentosFuturos = new ArrayList<>();
        List<LocalDate> datasConflito = new ArrayList<>();

        LocalDate dataInicio = agendamentoBase.getDataAgendamento();
        LocalDate dataAtual = dataInicio.plusWeeks(1); // Próxima semana

        while (!dataAtual.isAfter(dataLimite)) { // Usa o limite teórico passado como parâmetro
            log.info("RECORRENCIA LOOP: Verificando data: {}", dataAtual);
            try {
                if (verificarDisponibilidadeParaData(agendamentoBase, dataAtual)) {
                    // Passamos o objeto pai (ainda não salvo) para o construtor
                    log.info("RECORRENCIA SUCESSO: Data {} está livre.", dataAtual);
                    Agendamento novoAgendamento = criarAgendamentoFuturo(agendamentoBase, dataAtual, agendamentoFixoPai);
                    agendamentosFuturos.add(novoAgendamento);
                } else {
                    log.warn("RECORRENCIA FALHA: Conflito de slot/Disponibilidade na data: {}", dataAtual);
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
        Set<SlotHorario> slotsNecessarios = buscarSlotsCorrespondentesParaData(
                agendamentoBase.getSlotsHorario(), data);

        if (slotsNecessarios.size() != agendamentoBase.getSlotsHorario().size()) {
            log.error("PRE-VALIDATION ERRO: Slot(s) para {} não encontrados/configurados.", data);
            return false;
        }

        for (SlotHorario slot : slotsNecessarios) {
            // Verifica se o slot está fisicamente disponível (manutenção/indisponível)
            if (slot.getStatusDisponibilidade() == StatusDisponibilidade.MANUTENCAO ||
                    slot.getStatusDisponibilidade() == StatusDisponibilidade.INDISPONIVEL) {
                log.warn("PRE-VALIDATION FALHA: Slot ID {} está indisponível (Manutenção/Bloqueado) para {}", slot.getId(), data);
                return false;
            }
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
                log.error("PRE-VALIDATION FALHA: Slot ID {} já possui um agendamento conflitante em {}", slot.getId(), data);
                return false;
            }
        }

        return true;
    }

    private Set<SlotHorario> buscarSlotsCorrespondentesParaData(Set<SlotHorario> slotsOriginais,
                                                                LocalDate data) {
        DiaDaSemana diaSemana = DiaDaSemana.values()[data.getDayOfWeek().getValue() - 1];
        Set<SlotHorario> slotsCorrespondentes = new HashSet<>();

        for (SlotHorario slotOriginal : slotsOriginais) {
            List<SlotHorario> slotsCorrespondentesList = slotHorarioRepository
                    .findByIntervaloHorario_HorarioFuncionamento_DiaDaSemanaAndHorarioInicioAndHorarioFim(
                            diaSemana,
                            slotOriginal.getHorarioInicio(),
                            slotOriginal.getHorarioFim()
                    );

            if (!slotsCorrespondentesList.isEmpty()) {
                // Pegamos o PRIMEIRO slot e logamos um WARN se houver duplicidade
                if (slotsCorrespondentesList.size() > 1) {
                    log.warn("ATENÇÃO: Slot duplicado encontrado para {}. Usando o ID: {}", data, slotsCorrespondentesList.get(0).getId());
                }
                slotsCorrespondentes.add(slotsCorrespondentesList.get(0));
            } else {
                // Se o slot não foi encontrado para este dia da semana (falha de inventário), falha a pré-validação
                return new HashSet<>();
            }
        }

        return slotsCorrespondentes;
    }

    private Agendamento criarAgendamentoFuturo(Agendamento agendamentoBase,
                                               LocalDate novaData,
                                               AgendamentoFixo agendamentoFixo) {

        Set<SlotHorario> slotsCorrespondentes = buscarSlotsCorrespondentesParaData(
                agendamentoBase.getSlotsHorario(), novaData);

        Agendamento novoAgendamento = Agendamento.builder()
                .dataAgendamento(novaData)
                .esporte(agendamentoBase.getEsporte())
                .isFixo(true)
                .isPublico(false)
                .periodoAgendamentoFixo(agendamentoBase.getPeriodoAgendamentoFixo())
                .vagasDisponiveis(agendamentoBase.getVagasDisponiveis())
                .status(agendamentoBase.getStatus())
                .quadra(agendamentoBase.getQuadra())
                .atleta(agendamentoBase.getAtleta())
                .agendamentoFixo(agendamentoFixo)
                .asaasPaymentId(agendamentoBase.getAsaasPaymentId())
                .slotsHorario(slotsCorrespondentes)
                .build();

        novoAgendamento.criarSnapshot();

        return novoAgendamento;
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
    public AgendamentoFixo buscarPorId(Long id) {
        return agendamentoFixoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento fixo não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> preValidarAgendamentoFixo(Agendamento agendamentoBase) {

        if (!agendamentoBase.isFixo() || agendamentoBase.getPeriodoAgendamentoFixo() == null) {
            return Collections.emptyList();
        }

        LocalDate dataLimite = calcularDataFim(agendamentoBase.getDataAgendamento(), agendamentoBase.getPeriodoAgendamentoFixo());
        List<LocalDate> datasConflito = new ArrayList<>();

        LocalDate dataInicio = agendamentoBase.getDataAgendamento();
        LocalDate dataAtual = dataInicio.plusWeeks(1); // Começa a checar a partir da Semana 1

        log.info("PRE-VALIDATION: Iniciando pré-validação de agendamento fixo para a base: {}", dataInicio);

        while (!dataAtual.isAfter(dataLimite)) {
            try {
                if (!verificarDisponibilidadeParaData(agendamentoBase, dataAtual)) {
                    // Conflito encontrado (outro agendamento ou slot indisponível)
                    datasConflito.add(dataAtual);
                    log.warn("PRE-VALIDATION FALHA: Conflito de slot/Disponibilidade na data: {}", dataAtual);
                }
            } catch (Exception e) {
                log.error("PRE-VALIDATION ERRO: Erro ao processar data {}: {}", dataAtual, e.getMessage());
                datasConflito.add(dataAtual);
            }

            dataAtual = dataAtual.plusWeeks(1);
        }

        log.info("PRE-VALIDATION FINALIZADA: Encontrados {} conflitos.", datasConflito.size());

        return datasConflito;
    }
}
