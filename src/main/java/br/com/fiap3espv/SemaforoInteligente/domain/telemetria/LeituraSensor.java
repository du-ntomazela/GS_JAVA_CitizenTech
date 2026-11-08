package br.com.fiap3espv.SemaforoInteligente.domain.telemetria;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "LeituraSensor")
@Table(name = "leituras_sensores")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class LeituraSensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cruzamento")
    private Cruzamento cruzamento;

    @Enumerated(EnumType.STRING)
    private Via via;

    private int fila;

    @Column(name = "fluxo_por_minuto")
    private int fluxoPorMinuto;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    public LeituraSensor(Cruzamento cruzamento, DadosLeituraVia dados, LocalDateTime dataHora) {
        this.cruzamento = cruzamento;
        this.via = dados.via();
        this.fila = dados.fila();
        this.fluxoPorMinuto = dados.fluxoPorMinuto();
        this.dataHora = dataHora;
    }
}
