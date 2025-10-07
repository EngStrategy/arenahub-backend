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

    @ElementCollection(targetClass = TipoEsporte.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "quadra_tipo_quadra", joinColumns = @JoinColumn(name = "quadra_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_quadra", nullable = false)
    @Builder.Default
    private Set<TipoEsporte> tipoQuadra = new HashSet<>();

    private String descricao;

    @Enumerated(EnumType.STRING)
    private DuracaoReserva duracaoReserva;

    private boolean cobertura;

    @Column(name = "iluminacao_noturna")
    private boolean iluminacaoNoturna;

    @Builder.Default
    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HorarioFuncionamento> horariosFuncionamento = new HashSet<>();

    @ElementCollection(targetClass = MaterialEsportivo.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "quadra_materiais_fornecidos", joinColumns = @JoinColumn(name = "quadra_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "material_fornecido", nullable = false)
    @Builder.Default
    private Set<MaterialEsportivo> materiaisFornecidos = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "arena_id", nullable = false)
    private Arena arena;

    @Builder.Default
    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agendamento> agendamentos = new ArrayList<>();
}
