package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.TipoFeedback;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFeedback tipo;

//  SQL para ver a mensagem SELECT convert_from(lo_get(mensagem::oid), 'UTF8') AS mensagem_real FROM feedbacks;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataEnvio;

    @Builder.Default
    private boolean resolvido = false;
}