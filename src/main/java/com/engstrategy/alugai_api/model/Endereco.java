package com.engstrategy.alugai_api.model;

import jakarta.mail.MessagingException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {
    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String rua;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    public String toStringFormatado() {
        StringBuilder endereco = new StringBuilder();
        endereco.append(rua).append(", ").append(numero);

        if (complemento != null && !complemento.trim().isEmpty()) {
            endereco.append(" - ").append(complemento);
        }

        endereco.append("<br>").append(bairro).append(", ").append(cidade).append(" - ").append(estado);
        endereco.append("<br>CEP: ").append(cep);

        return endereco.toString();
    }
}
