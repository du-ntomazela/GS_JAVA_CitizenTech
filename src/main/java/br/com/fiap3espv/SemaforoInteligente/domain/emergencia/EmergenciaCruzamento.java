package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Um cruzamento da rota de uma emergencia, na ordem em que a ambulancia vai passar. */
@Entity(name = "EmergenciaCruzamento")
@Table(name = "emergencias_cruzamentos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class EmergenciaCruzamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emergencia")
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cruzamento")
    private Cruzamento cruzamento;

    private int ordem;

    @Column(name = "distancia_na_rota")
    private double distanciaNaRota;

    @Enumerated(EnumType.STRING)
    private Eixo eixo;

    @Enumerated(EnumType.STRING)
    private SituacaoCruzamentoRota situacao;

    @Column(name = "data_liberacao")
    private LocalDateTime dataLiberacao;

    @Column(name = "data_restauracao")
    private LocalDateTime dataRestauracao;

    public EmergenciaCruzamento(Emergencia emergencia, Cruzamento cruzamento, int ordem, double distanciaNaRota, Eixo eixo) {
        this.emergencia = emergencia;
        this.cruzamento = cruzamento;
        this.ordem = ordem;
        this.distanciaNaRota = distanciaNaRota;
        this.eixo = eixo;
        this.situacao = SituacaoCruzamentoRota.AGUARDANDO;
    }

    public void liberar() {
        this.situacao = SituacaoCruzamentoRota.LIBERADO;
        this.dataLiberacao = LocalDateTime.now();
    }

    public void restaurar() {
        this.situacao = SituacaoCruzamentoRota.RESTAURADO;
        this.dataRestauracao = LocalDateTime.now();
    }

    public boolean isAguardando() {
        return situacao == SituacaoCruzamentoRota.AGUARDANDO;
    }

    public boolean isLiberado() {
        return situacao == SituacaoCruzamentoRota.LIBERADO;
    }
}
