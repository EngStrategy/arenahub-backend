package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.agendamento.AgendamentoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.AgendamentoExternoCreateDTO;
import com.engstrategy.alugai_api.dto.agendamento.NovoAtletaExternoDTO;
import com.engstrategy.alugai_api.dto.agendamento.PixPagamentoResponseDTO;
import com.engstrategy.alugai_api.dto.asaas.*;
import com.engstrategy.alugai_api.exceptions.AccessDeniedException;
import com.engstrategy.alugai_api.exceptions.SubscriptionInactiveException;
import com.engstrategy.alugai_api.exceptions.UniqueConstraintViolationException;
import com.engstrategy.alugai_api.mapper.AgendamentoMapper;
import com.engstrategy.alugai_api.model.*;
import com.engstrategy.alugai_api.model.enums.*;
import com.engstrategy.alugai_api.repository.*;
import com.engstrategy.alugai_api.repository.specs.AgendamentoSpecs;
import com.engstrategy.alugai_api.service.AgendamentoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendamentoServiceImpl implements AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final AtletaRepository atletaRepository;
    private final SlotHorarioService slotHorarioService;
    private final SlotHorarioRepository slotHorarioRepository;
    private final AgendamentoMapper agendamentoMapper;
    private final EmailService emailService;
    private final QuadraRepository quadraRepository;
    private final ArenaRepository arenaRepository;
    private final AsaasService asaasService;
    private final ZoneId fusoHorarioPadrao = ZoneId.of("America/Sao_Paulo");

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private void validarStatusAssinaturaDaArena(Quadra quadra) {
        // Buscamos a Arena pelo ID para garantir que temos o objeto completo e atualizado
        Arena arena = arenaRepository.findById(quadra.getArena().getId())
                .orElseThrow(() -> new EntityNotFoundException("Arena associada à quadra não foi encontrada."));

        if (arena.getStatusAssinatura() != StatusAssinatura.ATIVA) {
            throw new SubscriptionInactiveException(
                    "Não é possível realizar agendamentos. A assinatura desta arena não está ativa."
            );
        }
    }

    @Override
    @Transactional
    public Agendamento criarAgendamento(AgendamentoCreateDTO dto, UUID atletaId) {
        log.info("Iniciando criação de agendamento para atleta ID: {} na data: {}",
                atletaId, dto.getDataAgendamento());

        // Validações de regras de negócio
        validarRegrasNegocio(dto);

        // Validar se a data não é no passado
        validarDataAgendamento(dto.getDataAgendamento());

        // Buscar e validar slots
        Set<SlotHorario> slots = buscarEValidarSlots(dto.getSlotHorarioIds());

        // Verifica se slots são subsequentes
        if (!slotHorarioService.saoSlotsSubsequentes(dto.getSlotHorarioIds())) {
            throw new IllegalArgumentException("Os horários selecionados devem ser subsequentes");
        }

        // Verificar disponibilidade dos slots na data específica
        verificarDisponibilidadeSlotsParaData(slots, dto.getDataAgendamento(), dto.getQuadraId());

        // Recuperar atleta que deseja realizar o agendamento
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado com ID: " + atletaId));

        // Recuperar quadra
        Quadra quadra = quadraRepository.findById(dto.getQuadraId())
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada com ID: " + dto.getQuadraId()));

        // Valida o status da assinatura da arena
        validarStatusAssinaturaDaArena(quadra);

        // Converter DTO para entidade
        Agendamento agendamento = agendamentoMapper.fromCreateToAgendamento(dto, slots, atleta);

        // Salvar o agendamento
        agendamento.criarSnapshot();
        agendamento = agendamentoRepository.save(agendamento);

        log.info("Agendamento criado com sucesso. ID: {}", agendamento.getId());

        emailService.enviarEmailAgendamento(atleta.getEmail(), atleta.getNome(), agendamento, Role.ATLETA);
        emailService.enviarEmailAgendamento(quadra.getArena().getEmail(), quadra.getArena().getNome(), agendamento, Role.ARENA);

        return agendamento;
    }

    private void validarRegrasNegocio(AgendamentoCreateDTO dto) {
        if (dto.isFixo() && dto.isPublico()) {
            throw new IllegalArgumentException("Um agendamento não pode ser fixo e público simultaneamente");
        }

        if (dto.isPublico() && dto.getNumeroJogadoresNecessarios() == null) {
            throw new IllegalArgumentException("Agendamentos públicos devem informar o número de jogadores necessários");
        }

        if (dto.isFixo() && dto.getPeriodoFixo() == null) {
            throw new IllegalArgumentException("Agendamentos fixos devem informar o período");
        }

        if (dto.isPublico() && (dto.getNumeroJogadoresNecessarios() == null || dto.getNumeroJogadoresNecessarios() <= 0)) {
            throw new IllegalArgumentException("Agendamentos públicos devem ter número de jogadores maior que zero");
        }
    }

    private void validarDataAgendamento(LocalDate dataAgendamento) {
        // Usar fuso horário de São Paulo para validação
        LocalDate dataAtual = LocalDate.now(fusoHorarioPadrao);

        if (dataAgendamento.isBefore(dataAtual)) {
            throw new IllegalArgumentException("Não é possível criar agendamentos para datas passadas");
        }

        // Opcional: limitar agendamentos muito distantes no futuro
        if (dataAgendamento.isAfter(dataAtual.plusYears(1))) {
            throw new IllegalArgumentException("Não é possível criar agendamentos com mais de 1 ano de antecedência");
        }
    }

    private Set<SlotHorario> buscarEValidarSlots(List<Long> slotIds) {
        if (slotIds == null || slotIds.isEmpty()) {
            throw new IllegalArgumentException("Deve ser informado pelo menos um slot de horário");
        }

        List<SlotHorario> slotsList = slotHorarioRepository.findAllById(slotIds);
        Set<SlotHorario> slots = new HashSet<>(slotsList);

        if (slots.size() != slotIds.size()) {
            throw new IllegalArgumentException("Um ou mais slots informados não foram encontrados");
        }

        return slots;
    }


    /**
     * Verifica se os slots estão disponíveis na data específica do agendamento
     */
    protected void verificarDisponibilidadeSlotsParaData(Set<SlotHorario> slots,
                                                         LocalDate dataAgendamento,
                                                         Long quadraId) {

        // Obter data e hora atual no fuso horário de São Paulo
        LocalDate dataAtual = LocalDate.now(fusoHorarioPadrao);
        LocalTime horaAtual = LocalTime.now(fusoHorarioPadrao);

        // Verificar se a data do agendamento é hoje
        boolean isDataAtual = dataAgendamento.equals(dataAtual);

        for (SlotHorario slot : slots) {
            // 1. Verificar se o slot está fisicamente disponível (não em manutenção)
            if (slot.getStatusDisponibilidade() == StatusDisponibilidade.MANUTENCAO ||
                    slot.getStatusDisponibilidade() == StatusDisponibilidade.INDISPONIVEL) {
                throw new IllegalArgumentException(
                        String.format("Slot %d (%s às %s) não está disponível - Status: %s",
                                slot.getId(),
                                slot.getHorarioInicio(),
                                slot.getHorarioFim(),
                                slot.getStatusDisponibilidade())
                );
            }

            // 2. Se a data é hoje, verificar se o horário do slot já passou
            if (isDataAtual && slot.getHorarioInicio().isBefore(horaAtual)) {
                throw new IllegalArgumentException(
                        String.format("Slot %d (%s às %s) não pode ser agendado pois o horário já passou. Horário atual: %s",
                                slot.getId(),
                                slot.getHorarioInicio(),
                                slot.getHorarioFim(),
                                horaAtual.format(DateTimeFormatter.ofPattern("HH:mm")))
                );
            }

            // 3. Verificar se já existe agendamento para este slot na data específica
            boolean jaAgendado = agendamentoRepository.existeConflito(
                    dataAgendamento,
                    quadraId,
                    slot.getHorarioInicio(),
                    slot.getHorarioFim()
            );

            if (jaAgendado) {
                throw new IllegalArgumentException(
                        String.format("Slot %d (%s às %s) já está ocupado na data %s",
                                slot.getId(),
                                slot.getHorarioInicio(),
                                slot.getHorarioFim(),
                                dataAgendamento)
                );
            }
        }
    }

    @Override
    @Transactional
    public void cancelarAgendamento(Long agendamentoId, UUID atletaId) {
        log.info("Cancelando agendamento ID: {} para atleta ID: {}", agendamentoId, atletaId);

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));

        validarPermissaoCancelamento(agendamento, atletaId);

        // Usar fuso horário de São Paulo para validação
        LocalDate dataAtual = LocalDate.now(fusoHorarioPadrao);
        LocalTime horaAtual = LocalTime.now(fusoHorarioPadrao);

        // Se a data do agendamento é hoje, verificar se o primeiro slot já passou
        if (agendamento.getDataAgendamento().equals(dataAtual)) {
            // Buscar o primeiro slot (horário de início mais cedo)
            LocalTime primeiroHorario = agendamento.getSlotsHorario().stream()
                    .map(SlotHorario::getHorarioInicio)
                    .min(LocalTime::compareTo)
                    .orElseThrow(() -> new IllegalStateException("Agendamento sem slots de horário"));

            if (primeiroHorario.isBefore(horaAtual)) {
                throw new IllegalArgumentException(
                        String.format("Não é possível cancelar agendamento pois o horário já passou. " +
                                        "Horário do agendamento: %s, Horário atual: %s",
                                primeiroHorario.format(DateTimeFormatter.ofPattern("HH:mm")),
                                horaAtual.format(DateTimeFormatter.ofPattern("HH:mm")))
                );
            }
        }

        Arena arena = agendamento.getQuadra().getArena();
        Integer horasParaCancelar = arena.getHorasCancelarAgendamento();

        if (horasParaCancelar == null) {
            horasParaCancelar = 24;
        }

        LocalDateTime inicioAgendamento = agendamento.getDataAgendamento().atTime(agendamento.getHorarioInicioSnapshot());

        LocalDateTime prazoCancelamento = inicioAgendamento.minusHours(horasParaCancelar);

        LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        if (agora.isAfter(prazoCancelamento)) {
            throw new IllegalArgumentException(
                    "O prazo para cancelamento expirou. O cancelamento deve ser feito com pelo menos " +
                            horasParaCancelar + " horas de antecedência."
            );
        }

        // Verificar se já não está cancelado
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new IllegalArgumentException("Agendamento já está cancelado");
        }

        // 1. Atualiza o status do agendamento
        agendamento.setStatus(StatusAgendamento.CANCELADO);

        // 2. Atualiza o status de todas as solicitações de entrada relacionadas
        if (agendamento.getSolicitacoes() != null) {
            for (SolicitacaoEntrada solicitacao : agendamento.getSolicitacoes()) {
                solicitacao.setStatus(StatusSolicitacao.CANCELADO);
            }
        }

        // 3. Salva o agendamento e, em cascata, as alterações nas solicitações
        agendamentoRepository.save(agendamento);

        // Envio de email para os participantes
        if (agendamento.isPublico() && agendamento.getParticipantes() != null) {
            for (Atleta participante : agendamento.getParticipantes()) {
                emailService.enviarEmailJogoCancelado(participante.getEmail(), participante.getNome(), agendamento);
            }
        }

        log.info("Agendamento cancelado com sucesso");
    }

    @Override
    public Page<Agendamento> buscarPorAtletaId(UUID atletaId,
                                               LocalDate dataInicio,
                                               LocalDate dataFim,
                                               TipoAgendamento tipoAgendamento,
                                               StatusAgendamento status,
                                               Pageable pageable) {


        Boolean isFixoFiltro = null;
        if (tipoAgendamento == TipoAgendamento.FIXO) {
            isFixoFiltro = Boolean.TRUE;
        } else if (tipoAgendamento == TipoAgendamento.NORMAL) {
            isFixoFiltro = Boolean.FALSE;
        }

        List<StatusAgendamento> statusFilter;

        if (status == StatusAgendamento.FINALIZADO) {
            statusFilter = Arrays.asList(StatusAgendamento.CANCELADO, StatusAgendamento.PAGO, StatusAgendamento.AUSENTE);
        } else if (status != null) {
            statusFilter = Arrays.asList(status);
        } else {
            statusFilter = Arrays.asList(
                    StatusAgendamento.PENDENTE,
                    StatusAgendamento.AGUARDANDO_PAGAMENTO,
                    StatusAgendamento.PAGO
            );
        }

        if (status == null && dataInicio == null) {
            dataInicio = LocalDate.now(fusoHorarioPadrao);
        }

        return agendamentoRepository.findByAtletaIdWithDetails(
                atletaId,
                dataInicio,
                dataFim,
                isFixoFiltro,
                statusFilter,
                pageable
        );
    }

    @Override
    public Agendamento buscarPorId(Long agendamentoId) {
        return agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
    }

    @Override
    public Page<Agendamento> buscarPorArenaId(UUID arenaId,
                                              LocalDate dataInicio,
                                              LocalDate dataFim,
                                              StatusAgendamento status,
                                              Long quadraId,
                                              Pageable pageable) {

        List<StatusAgendamento> statusesParaFiltrar = null; // null significa que não filtraremos por status

        if (status != null) {
            if (status == StatusAgendamento.FINALIZADO) {
                // Se o filtro for FINALIZADO, buscamos por todos estes status
                statusesParaFiltrar = Arrays.asList(StatusAgendamento.CANCELADO, StatusAgendamento.PAGO, StatusAgendamento.AUSENTE);
            } else {
                // Para qualquer outro status, buscamos apenas por ele
                statusesParaFiltrar = List.of(status);
            }
        }

        return agendamentoRepository.findByArenaIdWithFilters(
                arenaId,
                dataInicio,
                dataFim,
                quadraId,
                statusesParaFiltrar,
                pageable
        );
    }

    @Override
    @Transactional
    public Agendamento atualizarStatus(Long agendamentoId, UUID arenaId, StatusAgendamento novoStatus) {
        log.info("Atualizando status do agendamento ID: {} para {}", agendamentoId, novoStatus);

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));

        if (!agendamento.getQuadra().getArena().getId().equals(arenaId)) {
            throw new AccessDeniedException("Você não tem permissão para atualizar este agendamento.");
        }

        StatusAgendamento statusAtual = agendamento.getStatus();
        if (statusAtual == StatusAgendamento.CANCELADO
                || statusAtual == StatusAgendamento.PAGO
                || statusAtual == StatusAgendamento.AUSENTE) {
            throw new IllegalStateException("Não é possível alterar o status de um agendamento que já foi finalizado.");
        }

        if (novoStatus != StatusAgendamento.PAGO && novoStatus != StatusAgendamento.AUSENTE && novoStatus != StatusAgendamento.CANCELADO) {
            throw new IllegalArgumentException("A arena só pode alterar o status para PAGO, AUSENTE ou CANCELADO.");
        }

        // Envio de email para os participantes
        if (agendamento.isPublico() && agendamento.getParticipantes() != null) {
            for (Atleta participante : agendamento.getParticipantes()) {
                emailService.enviarEmailJogoCancelado(participante.getEmail(), participante.getNome(), agendamento);
            }
        }

        emailService.enviarEmailJogoCancelado(agendamento.getAtleta().getEmail(), agendamento.getAtleta().getNome(),
                agendamento);

        agendamento.setStatus(novoStatus);
        return agendamentoRepository.save(agendamento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agendamento> buscarAgendamentosParaAvaliacao(UUID atletaId) {
        LocalDate hoje = LocalDate.now(fusoHorarioPadrao);
        LocalTime agora = LocalTime.now(fusoHorarioPadrao);

        return agendamentoRepository.findAgendamentosPendentesDeAvaliacao(atletaId, hoje, agora);
    }


    private void validarPermissaoCancelamento(Agendamento agendamento, UUID userId) {
        if (!agendamento.getAtleta().getId().equals(userId) && !agendamento.getQuadra().getArena().getId().equals(userId)) {
            throw new AccessDeniedException("Usuário não autorizado!");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agendamento> buscarPendentesAcaoPorArenaId(UUID arenaId) {
        LocalDate dataAtual = LocalDate.now(fusoHorarioPadrao);
        LocalTime horaAtual = LocalTime.now(fusoHorarioPadrao);

        return agendamentoRepository.findPendentesAcaoByArenaId(arenaId, dataAtual, horaAtual);
    }

    @Override
    @Transactional
    public Agendamento criarAgendamentoExterno(AgendamentoExternoCreateDTO dto, UUID arenaId) {
        Atleta atleta;

        // Determina o atleta (existente ou novo)
        if (dto.getAtletaExistenteId() != null) {
            atleta = atletaRepository.findById(dto.getAtletaExistenteId())
                    .orElseThrow(() -> new EntityNotFoundException("Atleta existente não encontrado"));
        } else {
            // Cria um novo atleta externo
            NovoAtletaExternoDTO novoAtletaDto = dto.getNovoAtleta();
            if (atletaRepository.existsByTelefone(novoAtletaDto.getTelefone())) {
                throw new UniqueConstraintViolationException("Já existe um atleta com este telefone.");
            }
            atleta = Atleta.builder()
                    .nome(novoAtletaDto.getNome())
                    .telefone(novoAtletaDto.getTelefone())
                    .tipoConta(TipoContaAtleta.EXTERNO)
                    .role(Role.ATLETA)
                    .ativo(true) // Atletas externos já nascem ativos
                    .build();
            atleta = atletaRepository.save(atleta);
        }

        // Valida se a quadra pertence à arena logada
        Quadra quadra = quadraRepository.findById(dto.getQuadraId())
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada"));
        if (!quadra.getArena().getId().equals(arenaId)) {
            throw new AccessDeniedException("Esta quadra não pertence à sua arena.");
        }

        // Logica para determinar um esporte para o agendamento
        TipoEsporte esporteAgendado;
        Set<TipoEsporte> esportesDaQuadra = quadra.getTipoQuadra();

        if (esportesDaQuadra == null || esportesDaQuadra.isEmpty()) {
            throw new IllegalStateException("Esta quadra não tem nenhum esporte configurado e não pode ser agendada.");

        } else if (esportesDaQuadra.size() == 1) {
            // Se a quadra só tem 1 esporte, ele é selecionado automaticamente.
            esporteAgendado = esportesDaQuadra.iterator().next();

        } else {
            // Se a quadra tem múltiplos esportes, o DTO PRECISA especificar um.
            if (dto.getEsporte() == null) {
                throw new IllegalArgumentException("Esta quadra suporta múltiplos esportes. Especifique o esporte a ser agendado.");
            }
            if (!esportesDaQuadra.contains(dto.getEsporte())) {
                throw new IllegalArgumentException("O esporte '" + dto.getEsporte() + "' não é válido para esta quadra.");
            }
            esporteAgendado = dto.getEsporte();
        }

        // Converte o DTO externo para o DTO de criação padrão
        AgendamentoCreateDTO agendamentoCoreDto = agendamentoMapper.fromExternoToCreateDTO(dto);

        // Garante que o esporte correto seja usado
        agendamentoCoreDto.setEsporte(esporteAgendado);

        return this.criarAgendamento(agendamentoCoreDto, atleta.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public StatusAgendamento verificarStatus(Long agendamentoId) {
        return agendamentoRepository.findById(agendamentoId)
                .map(Agendamento::getStatus)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));
    }

    @Override
    @Transactional
    public PixPagamentoResponseDTO criarPagamentoPix(AgendamentoCreateDTO dto, UUID atletaId) {
        // CRIAR O AGENDAMENTO PROVISÓRIO COM STATUS 'AGUARDANDO_PAGAMENTO'
        log.info("Iniciando criação de pagamento PIX para atleta ID: {} na data: {}", atletaId, dto.getDataAgendamento());

        validarDataAgendamento(dto.getDataAgendamento());
        Set<SlotHorario> slots = buscarEValidarSlots(dto.getSlotHorarioIds());

        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));

        String cpfParaAsaas = atleta.getCpfCnpj();

        if (cpfParaAsaas == null || cpfParaAsaas.isBlank()) {
            cpfParaAsaas = dto.getCpfCnpjPagamento();

            if (cpfParaAsaas == null || cpfParaAsaas.isBlank()) {
                throw new IllegalArgumentException("CPF/CNPJ é obrigatório para gerar pagamento PIX.");

            }

            String cpfLimpo = cpfParaAsaas.replaceAll("[^0-9]", ""); // Garante que apenas números serão salvos

            // Atualiza a entidade Atleta com o CPF limpo
            atleta.setCpfCnpj(cpfLimpo);

            atletaRepository.save(atleta);

            cpfParaAsaas = cpfLimpo;
        } else {
            // Se o CPF já existe no cadastro, apenas limpamos para a API do Asaas.
            cpfParaAsaas = cpfParaAsaas.replaceAll("[^0-9]", "");
        }

        Quadra quadra = quadraRepository.findById(dto.getQuadraId())
                .orElseThrow(() -> new EntityNotFoundException("Quadra não encontrada"));

        validarStatusAssinaturaDaArena(quadra);

        Agendamento agendamento = agendamentoMapper.fromCreateToAgendamento(dto, slots, atleta);
        agendamento.setStatus(StatusAgendamento.AGUARDANDO_PAGAMENTO);
        agendamento.criarSnapshot();

        Agendamento agendamentoProvisorio = agendamentoRepository.save(agendamento);
        log.info("Agendamento provisório criado com ID: {}", agendamentoProvisorio.getId());

        try {
            String cpfLimpo = cpfParaAsaas.replaceAll("[^0-9]", "");

            String telefoneOriginal = atleta.getTelefone();
            String telefoneLimpo = (telefoneOriginal != null && !telefoneOriginal.isBlank())
                    ? telefoneOriginal.replaceAll("[^0-9]", "")
                    : null;

            String nomeLimpo = atleta.getNome().replaceAll("[^a-zA-ZáàâãéèêíìîóòôõúùûüçÇ\\s]", "");

            AsaasCreateCustomerRequest customerRequest = AsaasCreateCustomerRequest.builder()
                    .name(nomeLimpo)
                    .email(atleta.getEmail())
                    .phone(telefoneLimpo)
                    .cpfCnpj(cpfLimpo)
                    .build();
            AsaasCustomerResponse customer = asaasService.createCustomer(customerRequest);

            // 2. CRIAR A COBRANÇA PIX
            BigDecimal valorTotal = agendamentoProvisorio.getValorTotalSnapshot();
            String dataVencimento = LocalDate.now().toString();

            AsaasCreatePaymentRequest paymentRequest = AsaasCreatePaymentRequest.builder()
                    .customer(customer.getId())
                    .value(valorTotal)
                    .dueDate(dataVencimento)
                    .description("Agendamento de quadra #" + agendamentoProvisorio.getId())
                    .build();

            AsaasPaymentResponse paymentResponse = asaasService.createPixPayment(paymentRequest);

            agendamentoProvisorio.setAsaasPaymentId(paymentResponse.getId()); // Define o ID retornado
            agendamentoRepository.save(agendamentoProvisorio); // Salva o agendamento com o ID do Asaas

            AsaasPixQrCodeResponse pixDataResponse = asaasService.getPixQrCode(paymentResponse.getId());

            String dataExpiracaoStr = pixDataResponse.getExpirationDate(); // Agora pega a String
            String qrCodeDataString = pixDataResponse.getPayload(); // Copia e Cola
            String qrCodeBase64 = pixDataResponse.getQrCodeBase64(); // Nome do campo ajustado no DTO

            if (qrCodeDataString == null || qrCodeBase64 == null || dataExpiracaoStr == null) {
                log.error("Dados de PIX retornados do Asaas estão incompletos para o pagamento {}", paymentResponse.getId());
                throw new RuntimeException("Falha ao gerar QR Code PIX. Dados incompletos. (Data de expiração ausente)");
            }

            LocalDateTime expiraEmForcada = LocalDateTime.now(fusoHorarioPadrao).plusMinutes(10);
            String expiraEmFormatada = expiraEmForcada.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            return PixPagamentoResponseDTO.builder()
                    .agendamentoId(agendamentoProvisorio.getId())
                    .statusAgendamento(agendamentoProvisorio.getStatus().name())
                    .qrCodeData(qrCodeBase64) // Imagem Base64 do QR Code
                    .copiaECola(qrCodeDataString) // Código Copia e Cola
                    .expiraEm(expiraEmFormatada)
                    .build();

        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Erro 400 do Asaas: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Erro de validação do provedor de pagamento. Verifique o CPF/CNPJ.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar pagamento Pix junto ao nosso provedor.", e);
        }
    }
}
