package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Um cruzamento controlado por UM dispositivo de borda (Raspberry Pi),
 * que comanda os semaforos das quatro vias (norte, sul, leste e oeste).
 */
@Entity(name = "Cruzamento")
@Table(name = "cruzamentos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Cruzamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nome;

    @Embedded
    private Coordenada localizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_conexao")
    private StatusConexao statusConexao;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_operacao")
    private ModoOperacao modoOperacao;

    @Column(name = "ultima_comunicacao")
    private LocalDateTime ultimaComunicacao;

    private boolean ativo = true;

    public Cruzamento(DadosCadastroCruzamento dados) {
        this.codigo = dados.codigo();
        this.nome = dados.nome();
        this.localizacao = new Coordenada(dados.localizacao());
        this.statusConexao = StatusConexao.OFFLINE;
        this.modoOperacao = dados.modoOperacao() != null ? dados.modoOperacao() : ModoOperacao.ADAPTATIVO;
        this.ativo = true;
    }

    public void atualizarInformacoes(DadosAtualizacaoCruzamento dados) {
        if (dados.nome() != null && !dados.nome().isBlank()) {
            this.nome = dados.nome();
        }
        if (dados.localizacao() != null) {
            this.localizacao.atualizarInformacoes(dados.localizacao());
        }
    }

    public void registrarComunicacao(LocalDateTime dataHora) {
        this.ultimaComunicacao = dataHora;
        this.statusConexao = StatusConexao.ONLINE;
    }

    public void marcarOnline() {
        this.statusConexao = StatusConexao.ONLINE;
        this.ultimaComunicacao = LocalDateTime.now();
    }

    public void marcarOffline() {
        this.statusConexao = StatusConexao.OFFLINE;
    }

    public void alterarModo(ModoOperacao modo) {
        this.modoOperacao = modo;
    }

    public boolean isOnline() {
        return statusConexao == StatusConexao.ONLINE;
    }

    public void excluir() {
        this.ativo = false;
    }
}
