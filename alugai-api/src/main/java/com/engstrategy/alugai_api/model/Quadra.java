package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.DiaDaSemana;
import com.engstrategy.alugai_api.model.enums.DuracaoReserva;
import com.engstrategy.alugai_api.model.enums.MaterialEsportivo;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(targetClass = TipoEsporte.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "quadra_id"))
    @Enumerated(EnumType.STRING)
    private List<TipoEsporte> tipoQuadra;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private DuracaoReserva duracaoReserva;

    private boolean cobertura;

    @Column(name = "iluminacao_noturna")
    private boolean iluminacaoNoturna;

    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL)
    private List<HorarioFuncionamento> horariosFuncionamento = new ArrayList<>();

    @ElementCollection(targetClass = MaterialEsportivo.class)
    @CollectionTable(name = "materiais_quadra", joinColumns = @JoinColumn(name = "quadra_id"))
    @Enumerated(EnumType.STRING)
    private List<MaterialEsportivo> materiaisFornecidos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "arena_id", nullable = false)
    private Arena arena;

    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agendamento> agendamentos = new ArrayList<>();
}
