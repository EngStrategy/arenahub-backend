package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.exceptions.UnavailableDateTimeException;
import com.engstrategy.alugai_api.exceptions.UserNotFoundException;
import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Atleta;
import com.engstrategy.alugai_api.model.IntervaloHorario;
import com.engstrategy.alugai_api.model.Quadra;
import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.model.enums.StatusIntervalo;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
import com.engstrategy.alugai_api.repository.AtletaRepository;
import com.engstrategy.alugai_api.repository.IntervaloHorarioRepository;
import com.engstrategy.alugai_api.repository.QuadraRepository;
import com.engstrategy.alugai_api.service.AgendamentoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgendamentoServiceImpl implements AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AtletaRepository atletaRepository;
    private final QuadraRepository quadraRepository;
    private final IntervaloHorarioRepository intervaloHorarioRepository; // Adicionar o repositório
    private final EmailService emailService;

    @Override
    @Transactional
    public Agendamento criarAgendamento(AgendamentoCreateDTO dto, Long atletaId) {

        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new UserNotFoundException("Atleta não encontrado com ID: " + atletaId));

        IntervaloHorario intervalo = intervaloHorarioRepository.findById(dto.getIntervaloHorarioId())
                .orElseThrow(() -> new EntityNotFoundException("Intervalo de horário não encontrado com ID: " +
                        dto.getIntervaloHorarioId()));

        Quadra quadra = quadraRepository.findById(dto.getQuadraId())
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada com ID: " + dto.getQuadraId()));

        // Validar que o intervalo pertence mesmo à quadra informada
        if (!intervalo.getHorarioFuncionamento().getQuadra().getId().equals(quadra.getId())) {
            throw new IllegalArgumentException("O intervalo de horário informado não pertence à quadra selecionada.");
        }

        // Validação de data: Não permitir agendamentos em datas passadas
        if (dto.getDataAgendamento().isBefore(LocalDate.now())) {
            throw new UnavailableDateTimeException("Não é possível agendar em uma data passada.");
        }

        // Validação de Dia da Semana: A data do agendamento corresponde ao dia do HorarioFuncionamento?
        Map<DayOfWeek, DiaDaSemana> mapaDias = Map.of(
                java.time.DayOfWeek.MONDAY, DiaDaSemana.SEGUNDA,
                java.time.DayOfWeek.TUESDAY, DiaDaSemana.TERCA,
                java.time.DayOfWeek.WEDNESDAY, DiaDaSemana.QUARTA,
                java.time.DayOfWeek.THURSDAY, DiaDaSemana.QUINTA,
                java.time.DayOfWeek.FRIDAY, DiaDaSemana.SEXTA,
                java.time.DayOfWeek.SATURDAY, DiaDaSemana.SABADO,
                java.time.DayOfWeek.SUNDAY, DiaDaSemana.DOMINGO
        );

        DiaDaSemana diaDaSemanaEsperado = mapaDias.get(dto.getDataAgendamento().getDayOfWeek());
        DiaDaSemana diaDaSemanaDoIntervalo = intervalo.getHorarioFuncionamento().getDiaDaSemana();

        if (diaDaSemanaDoIntervalo != diaDaSemanaEsperado) {
            throw new UnavailableDateTimeException("A data do agendamento (" + diaDaSemanaEsperado +
                    ") não corresponde ao dia da semana do horário de funcionamento selecionado ("
                    + diaDaSemanaDoIntervalo + ").");
        }

        // Validar se o intervalo tá disponível
        if (intervalo.getStatus() != StatusIntervalo.DISPONIVEL) {
            throw new UnavailableDateTimeException("Este horário não está disponível para agendamento");
        }

        // Validação de Conflito
        boolean jaExisteAgendamento = agendamentoRepository.existsByQuadraAndDataAgendamentoAndInicioAndFim(
                quadra, dto.getDataAgendamento(), intervalo.getInicio(), intervalo.getFim()
        );

        if (jaExisteAgendamento) {
            throw new UnavailableDateTimeException("Horário já reservado.");
        }

        // Criar e salvar o novo agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setAtleta(atleta);
        agendamento.setQuadra(quadra);
        agendamento.setDataAgendamento(dto.getDataAgendamento());
        agendamento.setInicio(intervalo.getInicio());
        agendamento.setFim(intervalo.getFim());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setEsporte(dto.getEsporte());
        agendamento.setPrivado(!dto.isPublico());
        agendamento.setFixo(dto.isFixo());


        if (dto.isPublico()) {
            agendamento.setNumeroJogadoresNecessarios(dto.getNumeroJogadoresNecessarios());
        }

        if (dto.isFixo()) {
            agendamento.setPeriodoAgendamentoFixo(dto.getPeriodoAgendamentoFixo());
        }

        Agendamento savedAgendamento = agendamentoRepository.save(agendamento);


        emailService.enviarEmailAgendamento(atleta.getEmail(), atleta.getNome(), savedAgendamento);
        emailService.enviarEmailAgendamento(quadra.getArena().getEmail(), quadra.getArena().getNome(), savedAgendamento);

        return savedAgendamento;
    }

    @Override
    public Page<Agendamento> buscarPorAtletaId(Long atletaId, Pageable pageable) {
        return agendamentoRepository.findByAtletaId(atletaId, pageable);
    }
}
