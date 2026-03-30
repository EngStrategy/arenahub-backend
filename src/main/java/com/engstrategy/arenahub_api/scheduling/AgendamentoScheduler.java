package com.engstrategy.arenahub_api.scheduling;

import com.engstrategy.arenahub_api.model.Agendamento;
import com.engstrategy.arenahub_api.model.enums.StatusAgendamento;
import com.engstrategy.arenahub_api.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgendamentoScheduler {

    private final AgendamentoRepository agendamentoRepository;
    private final ZoneId fusoHorarioPadrao = ZoneId.of("America/Sao_Paulo");

    @Scheduled(fixedRate = 300000) // 5 minutos
    @Transactional
    public void cancelarAgendamentosBloqueadosExpirados() {
        log.info("Iniciando job de cancelamento de agendamentos bloqueados expirados...");
        
        LocalDateTime agora = LocalDateTime.now(fusoHorarioPadrao);
        
        List<Agendamento> expirados = agendamentoRepository.findAgendamentosBloqueadosExpirados(agora);
        
        if (!expirados.isEmpty()) {
            log.info("Encontrados {} agendamentos bloqueados expirados. Cancelando...", expirados.size());
            for (Agendamento agendamento : expirados) {
                agendamento.setStatus(StatusAgendamento.CANCELADO);
            }
            agendamentoRepository.saveAll(expirados);
        } else {
            log.info("Nenhum agendamento bloqueado expirado encontrado.");
        }
    }
}
