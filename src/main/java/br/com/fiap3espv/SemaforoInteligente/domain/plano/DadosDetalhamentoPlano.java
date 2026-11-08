package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DadosDetalhamentoPlano(
        Long id,
        String cruzamento,

        @JsonProperty("verde_norte_sul")
        int verdeNorteSul,

        @JsonProperty("verde_leste_oeste")
        int verdeLesteOeste,

        int amarelo,

        @JsonProperty("duracao_ciclo")
        int duracaoCiclo,

        OrigemPlano origem,
        String observacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        @JsonProperty("data_hora")
        LocalDateTime dataHora) {
    public DadosDetalhamentoPlano(PlanoSemaforico plano) {
        this(
                plano.getId(),
                plano.getCruzamento().getCodigo(),
                plano.getVerdeNorteSul(),
                plano.getVerdeLesteOeste(),
                plano.getAmarelo(),
                plano.getDuracaoCiclo(),
                plano.getOrigem(),
                plano.getObservacao(),
                plano.getDataHora()
        );
    }
}
