package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.Agendamento;
import com.engstrategy.alugai_api.model.enums.Role;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void enviarCodigoVerificacao(String destino, String nome, String codigo) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
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
            helper.setTo(destino);
            helper.setSubject("Confirmação de Agendamento - Alugaí");

            String content = htmlContentAgendamento(nome, agendamento, role);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email de agendamento enviado para {} ({})", destino, role);
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de agendamento para {}: {}", destino, e.getMessage());
            throw new RuntimeException("Erro ao enviar email de agendamento", e);
        }
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
                           margin: 15px 0;
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
                           <h1>ALUGAÍ.</h1>
                       </div>
                       <div class="content">
                           <h2>Olá, %s!</h2>
                           <p>Seu agendamento foi confirmado com sucesso! Abaixo estão os detalhes da sua reserva:</p>
                         \s
                           <div class="agendamento-info">
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
                           <p>Chegue com alguns minutos de antecedência e tenha uma excelente partida!</p>
                       </div>
                       <div class="footer">
                           <p>Em caso de dúvidas, entre em contato conosco.</p>
                           <p>Obrigado por usar o Alugaí!</p>
                       </div>
                   </div>
               </body>
               </html>
              \s""".formatted(nome,
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
                           margin: 15px 0;
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
                           <h1>ALUGAÍ.</h1>
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
                           <p>Prepare a quadra e garanta que tudo esteja pronto para receber o atleta!</p>
                       </div>
                       <div class="footer">
                           <p>Gerencie seus agendamentos através da plataforma Alugaí.</p>
                           <p>Obrigado por ser parceiro do Alugaí!</p>
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
                           <h1>ALUGAÍ.</h1>
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
                       <h1>ALUGAÍ.</h1>
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
