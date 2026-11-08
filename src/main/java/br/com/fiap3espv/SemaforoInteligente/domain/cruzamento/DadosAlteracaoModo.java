package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import jakarta.validation.constraints.NotNull;

public record DadosAlteracaoModo(
        @NotNull
        ModoOperacao modo) {
}
