package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoAmbulancia(
        @NotNull
        Long id,

        String base) {
}
