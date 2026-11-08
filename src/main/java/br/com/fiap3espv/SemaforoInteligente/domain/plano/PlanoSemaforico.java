package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Tempo de verde (em segundos) de cada fase de um cruzamento, enviado ao dispositivo de borda. */
@Entity(name = "PlanoSemaforico")
@Table(name = "planos_semaforicos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class PlanoSemaforico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cruzamento")
    private Cruzamento cruzamento;

    @Column(name = "verde_norte_sul")
    private int verdeNorteSul;

    @Column(name = "verde_leste_oeste")
    private int verdeLesteOeste;

    private int amarelo;

    @Enumerated(EnumType.STRING)
    private OrigemPlano origem;

    private String observacao;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    public PlanoSemaforico(Cruzamento cruzamento, int verdeNorteSul, int verdeLesteOeste, int amarelo,
                           OrigemPlano origem, String observacao) {
        this.cruzamento = cruzamento;
        this.verdeNorteSul = verdeNorteSul;
        this.verdeLesteOeste = verdeLesteOeste;
        this.amarelo = amarelo;
        this.origem = origem;
        this.observacao = observacao;
        this.dataHora = LocalDateTime.now();
    }

    public int getDuracaoCiclo() {
        return verdeNorteSul + verdeLesteOeste + 2 * amarelo;
    }
}
