package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DadosDetalhamentoCruzamento(
        Long id,
        String codigo,
        String nome,
        DadosCoordenada localizacao,

        @JsonProperty("status_conexao")
        StatusConexao statusConexao,

        @JsonProperty("modo_operacao")
        ModoOperacao modoOperacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        @JsonProperty("ultima_comunicacao")
        LocalDateTime ultimaComunicacao,

        boolean ativo) {
    public DadosDetalhamentoCruzamento(Cruzamento cruzamento) {
        this(
                cruzamento.getId(),
                cruzamento.getCodigo(),
                cruzamento.getNome(),
                new DadosCoordenada(cruzamento.getLocalizacao()),
                cruzamento.getStatusConexao(),
                cruzamento.getModoOperacao(),
                cruzamento.getUltimaComunicacao(),
                cruzamento.isAtivo()
        );
    }
}
