package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.engstrategy.alugai_api.model.enums.MaterialEsportivo;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quadra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_foto_quadra")
    private String urlFotoQuadra;

    @Column(name = "nome_quadra")
    private String nomeQuadra;

    @Builder.Default
    @ElementCollection(targetClass = TipoEsporte.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "quadra_id"))
    @Enumerated(EnumType.STRING)
    private List<TipoEsporte> tipoQuadra = new ArrayList<>();

    private String descricao;

    @Enumerated(EnumType.STRING)
    private DuracaoReserva duracaoReserva;

    private boolean cobertura;

    @Column(name = "iluminacao_noturna")
    private boolean iluminacaoNoturna;

    @Builder.Default
    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL)
    private Set<HorarioFuncionamento> horariosFuncionamento = new HashSet<>();

    @Builder.Default
    @ElementCollection(targetClass = MaterialEsportivo.class)
    @CollectionTable(name = "materiais_quadra", joinColumns = @JoinColumn(name = "quadra_id"))
    @Enumerated(EnumType.STRING)
    private List<MaterialEsportivo> materiaisFornecidos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "arena_id", nullable = false)
    private Arena arena;

    @Builder.Default
    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agendamento> agendamentos = new ArrayList<>();
}
