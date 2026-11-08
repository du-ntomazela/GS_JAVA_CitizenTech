package br.com.fiap3espv.SemaforoInteligente.domain.telemetria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DadosDetalhamentoLeitura(
        Long id,
        String cruzamento,
        Via via,
        int fila,

        @JsonProperty("fluxo_por_minuto")
        int fluxoPorMinuto,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        @JsonProperty("data_hora")
        LocalDateTime dataHora) {
    public DadosDetalhamentoLeitura(LeituraSensor leitura) {
        this(
                leitura.getId(),
                leitura.getCruzamento().getCodigo(),
                leitura.getVia(),
                leitura.getFila(),
                leitura.getFluxoPorMinuto(),
                leitura.getDataHora()
        );
    }
}
