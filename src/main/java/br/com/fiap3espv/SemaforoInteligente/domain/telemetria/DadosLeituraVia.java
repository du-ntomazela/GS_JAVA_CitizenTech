package br.com.fiap3espv.SemaforoInteligente.domain.telemetria;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DadosLeituraVia(
        @NotNull
        Via via,

        @NotNull
        @Min(0)
        @Max(500)
        Integer fila,

        @NotNull
        @Min(0)
        @Max(500)
        @JsonAlias("fluxo_por_minuto")
        Integer fluxoPorMinuto) {
}
