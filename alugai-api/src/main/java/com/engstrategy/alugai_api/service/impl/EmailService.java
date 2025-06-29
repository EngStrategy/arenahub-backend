package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.model.Agendamento;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
    public void enviarEmailAgendamento(String destino, String nome, Agendamento agendamento) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        try {
            helper.setTo(destino);
            helper.setSubject("Confirmação de Agendamento - Alugaí");
            String content = "Olá, " + nome + "!<br>" +
                    "Seu agendamento foi confirmado com sucesso.<br><br>" +
                    "<b>Detalhes:</b><br>" +
                    "<b>Quadra:</b> " + agendamento.getQuadra().getNomeQuadra() + "<br>" +
                    "<b>Data:</b> " + agendamento.getDataAgendamento().toString() + "<br>" +
                    "<b>Horário:</b> " + agendamento.getInicio().toString() + " - " + agendamento.getFim().toString() + "<br><br>" +
                    "Obrigado por usar o Alugaí!";
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de agendamento", e);
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
