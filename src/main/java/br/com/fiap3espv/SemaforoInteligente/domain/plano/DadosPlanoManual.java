package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Intervencao manual da operadora de transito. */
public record DadosPlanoManual(
        @NotNull
        @Min(10)
        @Max(180)
        @JsonAlias("verde_norte_sul")
        Integer verdeNorteSul,

        @NotNull
        @Min(10)
        @Max(180)
        @JsonAlias("verde_leste_oeste")
        Integer verdeLesteOeste,

        @Size(max = 255)
        String observacao) {
}
