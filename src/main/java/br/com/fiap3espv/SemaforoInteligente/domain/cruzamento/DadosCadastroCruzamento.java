package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroCruzamento(
        @NotBlank
        @Pattern(regexp = "[a-z0-9-]{3,50}", message = "deve conter apenas letras minúsculas, números e hífen (ex: cruzamento-13)")
        String codigo,

        @NotBlank
        String nome,

        @NotNull
        @Valid
        DadosCoordenada localizacao,

        ModoOperacao modoOperacao) {
}
