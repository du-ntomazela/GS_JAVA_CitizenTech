package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DadosListagemCruzamentos(
        Long id,
        String codigo,
        String nome,
        Double latitude,
        Double longitude,

        @JsonProperty("status_conexao")
        StatusConexao statusConexao,

        @JsonProperty("modo_operacao")
        ModoOperacao modoOperacao) {
    public DadosListagemCruzamentos(Cruzamento cruzamento) {
        this(
                cruzamento.getId(),
                cruzamento.getCodigo(),
                cruzamento.getNome(),
                cruzamento.getLocalizacao().getLatitude(),
                cruzamento.getLocalizacao().getLongitude(),
                cruzamento.getStatusConexao(),
                cruzamento.getModoOperacao()
        );
    }
}
