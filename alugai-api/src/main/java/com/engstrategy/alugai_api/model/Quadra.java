package com.engstrategy.alugai_api.model;

import com.engstrategy.alugai_api.model.enums.CobrancaPeriodo;
import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Quadra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private TipoEsporte tipoQuadra;
    private String descricao;
    private boolean cobertura;
    private boolean iluminacaoNoturna;
    private String locacaoObjetos;
    private CobrancaPeriodo cobrancaPeriodo;

    @ElementCollection
    @CollectionTable(name = "quadra_horarios", joinColumns = @JoinColumn(name = "quadra_id"))
    private List<HorarioFuncionamento> horariosFuncionamento = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "arena_id", nullable = false)
    private Arena arena;

    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Agendamento> agendamentos = new ArrayList<>();
}
