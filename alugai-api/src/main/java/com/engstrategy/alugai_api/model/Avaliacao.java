package com.engstrategy.alugai_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int nota; // Ex: 1 a 5 estrelas

    @Lob // Para textos mais longos
    private String comentario;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dataAvaliacao = LocalDateTime.now();

    // Uma avaliação pertence a UM agendamento
    @OneToOne
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    // Uma avaliação é feita por UM atleta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;
}