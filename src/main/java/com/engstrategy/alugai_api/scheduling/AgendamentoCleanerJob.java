package com.engstrategy.alugai_api.scheduling;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.StatusAgendamento;
import com.engstrategy.alugai_api.repository.AgendamentoRepository;
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
        LocalDateTime limite = LocalDateTime.now().minusMinutes(10);

        // Encontra agendamentos que estão AGUARDANDO_PAGAMENTO E criados antes do limite
        List<Agendamento> agendamentosExpirados = agendamentoRepository.findExpirados(
                StatusAgendamento.AGUARDANDO_PAGAMENTO,
                limite
        );

        for (Agendamento agendamento : agendamentosExpirados) {
            agendamento.setStatus(StatusAgendamento.CANCELADO); // Marcar como CANCELADO
            log.info("Agendamento {} expirado e cancelado.", agendamento.getId());
        }
        agendamentoRepository.saveAll(agendamentosExpirados);
    }
}