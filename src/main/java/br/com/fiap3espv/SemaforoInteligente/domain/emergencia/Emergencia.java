package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.Ambulancia;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Emergencia")
@Table(name = "emergencias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Emergencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ambulancia")
    private Ambulancia ambulancia;

    private String socorrista;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "origem_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "origem_longitude"))
    })
    private Coordenada origem;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "destino_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "destino_longitude"))
    })
    private Coordenada destino;

    @Column(name = "descricao_destino")
    private String descricaoDestino;

    @Enumerated(EnumType.STRING)
    private StatusEmergencia status;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "ultima_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "ultima_longitude"))
    })
    private Coordenada ultimaPosicao;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @OneToMany(mappedBy = "emergencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem asc")
    private List<EmergenciaCruzamento> rota = new ArrayList<>();

    public Emergencia(Ambulancia ambulancia, String socorrista, DadosAberturaEmergencia dados) {
        this.ambulancia = ambulancia;
        this.socorrista = socorrista;
        this.origem = new Coordenada(dados.origem());
        this.destino = new Coordenada(dados.destino());
        this.descricaoDestino = dados.descricaoDestino();
        this.status = StatusEmergencia.ATIVA;
        this.dataInicio = LocalDateTime.now();
        this.ultimaPosicao = new Coordenada(this.origem.getLatitude(), this.origem.getLongitude());
        this.ultimaAtualizacao = this.dataInicio;
        this.rota = new ArrayList<>();
    }

    public void adicionarNaRota(EmergenciaCruzamento item) {
        this.rota.add(item);
    }

    public void registrarPosicao(Coordenada posicao, LocalDateTime dataHora) {
        this.ultimaPosicao = posicao;
        this.ultimaAtualizacao = dataHora;
    }

    public void encerrar() {
        this.status = StatusEmergencia.ENCERRADA;
        this.dataFim = LocalDateTime.now();
    }

    public boolean isAtiva() {
        return status == StatusEmergencia.ATIVA;
    }

    public long getSemaforosLiberados() {
        return rota.stream().filter(r -> r.getDataLiberacao() != null).count();
    }
}
