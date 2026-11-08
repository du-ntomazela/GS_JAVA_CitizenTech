package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.plano.DadosDetalhamentoPlano;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DadosProcessamentoTelemetria(
        String cruzamento,

        @JsonProperty("leituras_registradas")
        int leiturasRegistradas,

        @JsonProperty("plano_aplicado")
        DadosDetalhamentoPlano planoAplicado) {
}
