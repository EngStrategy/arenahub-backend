package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.Feedback;
import com.engstrategy.alugai_api.model.enums.Role;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${arenahub.admin-email}")
    private String adminEmail;

    @Async
    public void enviarCodigoVerificacao(String destino, String nome, String codigo) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setFrom(senderEmail);
            helper.setTo(destino);
            helper.setSubject("Código de Verificação");
            helper.setText(htmlContent(nome, codigo), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    @Async
    public void enviarCodigoResetSenha(String destino, String nome, String codigo) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setFrom(senderEmail);
            helper.setTo(destino);
            helper.setSubject("Redefinição de Senha");
            helper.setText(htmlContentResetSenha(nome, codigo), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    @Async
    public void enviarEmailAgendamento(String destino, String nome, Agendamento agendamento, Role role) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setFrom(senderEmail);
            helper.setTo(destino);
            helper.setSubject("Confirmação de Agendamento - ArenaHub");

            String content = htmlContentAgendamento(nome, agendamento, role);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email de agendamento enviado para {} ({})", destino, role);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de agendamento para {}: {}", destino, e.getMessage());
            throw new RuntimeException("Erro ao enviar email de agendamento", e);
        }
    }

    @Async
    public void enviarEmailNovaSolicitacao(String emailDono, String nomeDono, String nomeSolicitante, Agendamento agendamento) {
        String dataFormatada = agendamento.getDataAgendamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String titulo = "Nova solicitação para o seu jogo!";
        String corpo = String.format("""
                <p>Olá, %s!</p>
                <p>O atleta <strong>%s</strong> quer participar do seu jogo de %s no dia %s.</p>
                <p>Acesse o app para aprovar ou recusar a solicitação.</p>
                """, nomeDono, nomeSolicitante, agendamento.getEsporte().getApelido(), dataFormatada);

        enviarEmailGenerico(emailDono, titulo, corpo, nomeDono);
    }

    @Async
    public void enviarEmailSolicitacaoAceita(String emailSolicitante, String nomeSolicitante, Agendamento agendamento) {
        String dataFormatada = agendamento.getDataAgendamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String titulo = "Você está dentro! Participação confirmada.";
        String corpo = String.format("""
                <p>Parabéns, %s!</p>
                <p>Sua entrada no jogo de <strong>%s</strong> do dia <strong>%s</strong> na arena <strong>%s</strong> foi aceita.</p>
                <p>Prepare o uniforme e tenha uma ótima partida!</p>
                """, nomeSolicitante, agendamento.getEsporte().getApelido(), dataFormatada, agendamento.getQuadra().getArena().getNome());

        enviarEmailGenerico(emailSolicitante, titulo, corpo, nomeSolicitante);
    }

    @Async
    public void enviarEmailSolicitacaoRecusada(String emailSolicitante, String nomeSolicitante, Agendamento agendamento) {
        String dataFormatada = agendamento.getDataAgendamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String titulo = "Atualização sobre sua solicitação de jogo";
        String corpo = String.format("""
                <p>Olá, %s.</p>
                <p>Infelizmente, sua solicitação para o jogo de <strong>%s</strong> no dia <strong>%s</strong> não foi aceita pelo organizador.</p>
                <p>Mas não desanime, procure outros jogos abertos em nossa plataforma!</p>
                """, nomeSolicitante, agendamento.getEsporte().getApelido(), dataFormatada);

        enviarEmailGenerico(emailSolicitante, titulo, corpo, nomeSolicitante);
    }

    @Async
    public void enviarEmailParticipanteSaiu(String emailDono, String nomeDono, String nomeParticipante, Agendamento agendamento) {
        String dataFormatada = agendamento.getDataAgendamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String titulo = "Um participante saiu do seu jogo";
        String corpo = String.format("""
                <p>Olá, %s.</p>
                <p>O atleta <strong>%s</strong> cancelou a participação no seu jogo de <strong>%s</strong> do dia <strong>%s</strong>.</p>
                <p>Uma nova vaga foi aberta.</p>
                """, nomeDono, nomeParticipante, agendamento.getEsporte().getApelido(), dataFormatada);

        enviarEmailGenerico(emailDono, titulo, corpo, nomeDono);
    }

    @Async
    public void enviarEmailJogoCancelado(String emailParticipante, String nomeParticipante, Agendamento agendamento) {
        String dataFormatada = agendamento.getDataAgendamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String titulo = "Atenção: Jogo cancelado!";
        String corpo = String.format("""
                <p>Olá, %s.</p>
                <p>O jogo de <strong>%s</strong> do dia <strong>%s</strong>, do qual você iria participar, foi <strong>cancelado</strong> pelo organizador.</p>
                <p>Sentimos muito pelo inconveniente.</p>
                """, nomeParticipante, agendamento.getEsporte().getApelido(), dataFormatada);

        enviarEmailGenerico(emailParticipante, titulo, corpo, nomeParticipante);
    }

    private void enviarEmailGenerico(String destinatario, String titulo, String corpo, String nomeDestinatario) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setFrom(senderEmail);
            helper.setTo(destinatario);
            helper.setSubject(titulo + " - ArenaHub");
            String htmlCompleto = htmlTemplateBase(titulo, corpo, nomeDestinatario);
            helper.setText(htmlCompleto, true);
            mailSender.send(message);
            log.info("Email '{}' enviado para {}", titulo, destinatario);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email '{}' para {}: {}", titulo, destinatario, e.getMessage());
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    private String htmlTemplateBase(String titulo, String corpo, String nome) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #15A01A; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px 20px; background-color: #f9f9f9; }
                        .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>ArenaHub.</h1></div>
                        <div class="content">
                            <h2>%s</h2>
                            %s
                        </div>
                        <div class="footer">
                            <p>Obrigado por usar o ArenaHub!</p>
                        </div>
                    </div>
                </body>
                </html>
                """, titulo, titulo, corpo);
    }


    private String htmlContentAgendamento(String nome, Agendamento agendamento, Role role) {
        String dataFormatada = agendamento.getDataAgendamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        if (role == Role.ATLETA) {
            return """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <meta name="author" content="rian-lima">
                                <title>Confirmação de Agendamento</title>
                                <style>
                                    body {
                                        font-family: Arial, sans-serif; line-height: 1.6; color: #333;
                                    }
                                  \s
                                    .container {
                                        max-width: 600px; margin: 0 auto; padding: 20px;
                                    }
                    \s
                                    .header {
                                        background-color: #15A01A; color: white; padding: 20px; text-align: center;
                                    }
                                  \s
                                    .content {
                                        padding: 30px 20px; background-color: #f9f9f9;
                                    }
                    \s
                                    .agendamento-info {
                                        background-color: #e8f5e8;
                                        padding: 20px;
                                        border-radius: 8px;
                                        margin: 20px 0;
                                    }
                                   \s
                                    .detail-item {
                                        margin: 8px 0;
                                        padding: 5px 0;
                                        border-bottom: 1px solid #ddd;
                                    }
                                   \s
                                    .detail-label {
                                        font-weight: bold;
                                        color: #15A01A;
                                    }
                                   \s
                                   .endereco-item {
                                       margin: 8px 0;
                                       padding: 5px 0;
                                       border-bottom: 1px solid #ddd;
                                       line-height: 1.4;
                                   }
                                   \s
                                    .valor-total {
                                       font-size: 18px;
                                       font-weight: bold;
                                       color: #15A01A;
                                       text-align: center;
                                       margin-top: 15px;
                                       background-color: white;
                                       padding: 12px;
                                       border-radius: 5px;
                                    }
                                  \s
                                    .footer {
                                        padding: 20px; text-align: center; color: #666; font-size: 12px;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>ArenaHub.</h1>
                                    </div>
                                    <div class="content">
                                        <h2>Olá, %s!</h2>
                                        <p>Seu agendamento foi confirmado com sucesso! Abaixo estão os detalhes da sua reserva:</p>
                                      \s
                                        <div class="agendamento-info">
                                            <div class="detail-item">
                                                <span class="detail-label">Arena:</span> %s
                                            </div>
                                            <div class="detail-item">
                                                <span class="detail-label">Quadra:</span> %s
                                            </div>
                                            <div class="detail-item">
                                                <span class="detail-label">Data:</span> %s
                                            </div>
                                            <div class="detail-item">
                                                <span class="detail-label">Horário:</span> %s às %s
                                            </div>
                                            <div class="detail-item">
                                                <span class="detail-label">Esporte:</span> %s
                                            </div>
                                            <div class="endereco-item">
                                                <span class="detail-label">Endereço:</span><br> %s
                                            </div>
                                           \s
                                            <div class="valor-total">
                                                Valor Total: R$ %s
                                            </div>
                                        </div>
                                       \s
                                        <h4>Chegue com alguns minutos de antecedência e tenha uma excelente partida!</h4>
                                    </div>
                                    <div class="footer">
                                        <p>Em caso de dúvidas, entre em contato conosco.</p>
                                        <p>Obrigado por usar o ArenaHub!</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                           \s""".formatted(nome,
                    agendamento.getQuadra().getArena().getNome(),
                    agendamento.getQuadra().getNomeQuadra(),
                    dataFormatada,
                    agendamento.getHorarioInicio().toString(),
                    agendamento.getHorarioFim().toString(),
                    agendamento.getEsporte().getApelido(),
                    agendamento.getQuadra().getArena().getEndereco().toStringFormatado(),
                    agendamento.getValorTotal().toString());
        } else {
            // Template para ARENA
            return """
                     <!DOCTYPE html>
                     <html>
                     <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <meta name="author" content="rian-lima">
                         <title>Novo Agendamento</title>
                         <style>
                             body {
                                 font-family: Arial, sans-serif; line-height: 1.6; color: #333;
                             }
                            \s
                             .container {
                                 max-width: 600px; margin: 0 auto; padding: 20px;
                             }
                            \s
                             .header {
                                 background-color: #15A01A; color: white; padding: 20px; text-align: center;
                             }
                            \s
                             .content {
                                 padding: 30px 20px; background-color: #f9f9f9;
                             }
                            \s
                             .agendamento-info {
                                 background-color: #e8f5e8;
                                 padding: 20px;
                                 border-radius: 8px;
                                 margin: 20px 0;
                             }
                            \s
                             .detail-item {
                                 margin: 8px 0;
                                 padding: 5px 0;
                                 border-bottom: 1px solid #ddd;
                             }
                            \s
                             .detail-label {
                                 font-weight: bold;
                                 color: #15A01A;
                             }
                            \s
                             .valor-total {
                                 font-size: 18px;
                                 font-weight: bold;
                                 color: #15A01A;
                                 text-align: center;
                                 margin-top: 15px;
                                 background-color: white;
                                 padding: 12px;
                                 border-radius: 5px;
                             }
                            \s
                             .footer {
                                 padding: 20px; text-align: center; color: #666; font-size: 12px;
                             }
                         </style>
                     </head>
                     <body>
                         <div class="container">
                             <div class="header">
                                 <h1>ArenaHub.</h1>
                             </div>
                             <div class="content">
                                 <h2>Olá, %s!</h2>
                                 <p>Você tem um novo agendamento em sua arena. Confira os detalhes abaixo:</p>
                               \s
                                 <div class="agendamento-info">
                                     <div class="detail-item">
                                         <span class="detail-label">Atleta:</span> %s
                                     </div>
                                     <div class="detail-item">
                                         <span class="detail-label">Quadra:</span> %s
                                     </div>
                                     <div class="detail-item">
                                         <span class="detail-label">Data:</span> %s
                                     </div>
                                     <div class="detail-item">
                                         <span class="detail-label">Horário:</span> %s às %s
                                     </div>
                                     <div class="detail-item">
                                         <span class="detail-label">Esporte:</span> %s
                                     </div>
                                    \s
                                     <div class="valor-total">
                                         Valor Total: R$ %s
                                     </div>
                                 </div>
                                \s
                                 <h4>Prepare a quadra e garanta que tudo esteja pronto para receber o atleta!</h4>
                             </div>
                             <div class="footer">
                                 <p>Gerencie seus agendamentos através da plataforma ArenaHub.</p>
                                 <p>Obrigado por ser parceiro do ArenaHub!</p>
                             </div>
                         </div>
                     </body>
                     </html>
                    \s""".formatted(nome,
                    agendamento.getAtleta().getNome(),
                    agendamento.getQuadra().getNomeQuadra(),
                    dataFormatada,
                    agendamento.getHorarioInicio().toString(),
                    agendamento.getHorarioFim().toString(),
                    agendamento.getEsporte().getApelido(),
                    agendamento.getValorTotal().toString());
        }
    }

    @Async
    public void notificarAdminNovoFeedback(Feedback feedback) {
        // Formata o tipo de feedback para uma apresentação mais amigável
        String tipoFeedbackFormatado = feedback.getTipo().toString().replace("_", " ").toLowerCase();
        tipoFeedbackFormatado = Character.toUpperCase(tipoFeedbackFormatado.charAt(0)) + tipoFeedbackFormatado.substring(1);

        String titulo = "Novo Feedback Recebido: " + tipoFeedbackFormatado;

        // Formata a data para o padrão brasileiro
        String dataFormatada = feedback.getDataEnvio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));

        // Substitui quebras de linha na mensagem por <br> para funcionar no HTML
        String mensagemFormatada = feedback.getMensagem().replace("\n", "<br>");

        // Monta o corpo do e-mail em HTML
        String corpo = String.format("""
                        <p>Um novo feedback foi enviado através da plataforma.</p>
                        <div style="border-left: 3px solid #ccc; padding-left: 15px; margin: 20px 0;">
                            <p><strong>De:</strong> %s (%s)</p>
                            <p><strong>Tipo:</strong> %s</p>
                            <p><strong>Enviado em:</strong> %s</p>
                            <p><strong>Mensagem:</strong></p>
                            <blockquote style="margin: 0; padding: 10px; background-color: #fff; border-radius: 5px;">
                                %s
                            </blockquote>
                        </div>
                        """,
                feedback.getNome(),
                feedback.getEmail(),
                tipoFeedbackFormatado,
                dataFormatada,
                mensagemFormatada
        );

        enviarEmailGenerico(adminEmail, titulo, corpo, "Equipe ArenaHub");
    }

    private String htmlContent(String nome, String codigo) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="author" content="rian-lima">
                    <title>Confirmação de Email</title>
                    <style>
                        body {\s
                            font-family: Arial, sans-serif; line-height: 1.6; color: #333;\s
                        }
                       \s
                        .container {\s
                            max-width: 600px; margin: 0 auto; padding: 20px;\s
                        }
                
                        .header {\s
                            background-color: #15A01A; color: white; padding: 20px; text-align: center;\s
                        }
                       \s
                        .content {\s
                            padding: 30px 20px; background-color: #f9f9f9;\s
                        }
                
                        .code {
                            background-color: #e2e2e2;
                            font-weight: bold;
                            letter-spacing: 8px;
                            font-size: 30px;
                            padding: 40px;
                            text-align: center;
                        }
                       \s
                        .footer {\s
                            padding: 20px; text-align: center; color: #666; font-size: 12px;\s
                        }
                
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>ArenaHub.</h1>
                        </div>
                        <div class="content">
                            <h2>Olá, %s!</h2>
                            <p>Obrigado por se cadastrar em nossa plataforma. Para ativar sua conta, digite o código abaixo na página de confirmação:</p>
                           \s
                            <p class="code">
                                %s
                            </p>
                
                            <p><strong>Este código expira em 15 minutos.</strong></p>
                        </div>
                        <div class="footer">
                            <p>Se você não criou esta conta, ignore este email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nome, codigo);
    }

    private String htmlContentResetSenha(String nome, String codigo) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="author" content="rian-lima">
                    <title>Reset de Senha</title>
                    <style>
                        body {\s
                            font-family: Arial, sans-serif; line-height: 1.6; color: #333;\s
                        }
                       \s
                        .container {\s
                            max-width: 600px; margin: 0 auto; padding: 20px;\s
                        }
                
                        .header {\s
                            background-color: #15A01A; color: white; padding: 20px; text-align: center;\s
                        }
                       \s
                        .content {\s
                            padding: 30px 20px; background-color: #f9f9f9;\s
                        }
                
                        .code {
                            background-color: #e2e2e2;
                            font-weight: bold;
                            letter-spacing: 8px;
                            font-size: 30px;
                            padding: 40px;
                            text-align: center;
                        }
                       \s
                        .footer {\s
                            padding: 20px; text-align: center; color: #666; font-size: 12px;\s
                        }
                
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>ArenaHub.</h1>
                        </div>
                        <div class="content">
                            <h2>Olá, %s!</h2>
                            <p>Recebemos uma solicitação para redefinir sua senha. Use o código abaixo para prosseguir:</p>
                           \s
                            <p class="code">
                                %s
                            </p>
                
                            <p><strong>Este código expira em 15 minutos.</strong></p>
                        </div>
                        <div class="footer">
                            <p>Se você não solicitou esta alteração, ignore este email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nome, codigo);
    }
}
