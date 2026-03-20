package com.engstrategy.arenahub_api.scheduling;

import com.engstrategy.arenahub_api.model.Agendamento;
import com.engstrategy.arenahub_api.model.enums.StatusAgendamento;
import com.engstrategy.arenahub_api.repository.AgendamentoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class AgendamentoCleanerJob {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    // Roda a cada 5 minutos
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void liberarHorariosExpirados() {
        LocalDateTime agora = LocalDateTime.now();

        // Encontra agendamentos que estão AGUARDANDO_PAGAMENTO e cujo bloqueio expirou.
        List<Agendamento> agendamentosExpirados = agendamentoRepository.findExpirados(
                StatusAgendamento.AGUARDANDO_PAGAMENTO,
                agora
        );

        for (Agendamento agendamento : agendamentosExpirados) {
            if (Boolean.TRUE.equals(agendamento.getPagamentoConfirmadoGateway())) {
                continue;
            }

            agendamento.setStatus(StatusAgendamento.CANCELADO);
            log.info("Agendamento {} expirado e cancelado.", agendamento.getId());
        }
        agendamentoRepository.saveAll(agendamentosExpirados);
    }
}
